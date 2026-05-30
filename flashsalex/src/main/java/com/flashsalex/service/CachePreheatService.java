package com.flashsalex.service;

import com.flashsalex.common.BusinessException;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.common.RedisConstants;
import com.flashsalex.dto.response.ActivityDetailResponse;
import com.flashsalex.entity.SeckillActivity;
import com.flashsalex.entity.SeckillStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存预热服务
 * 将秒杀活动数据和库存写入 Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachePreheatService {

    private final SeckillActivityService activityService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 预热指定活动
     * 将活动详情和库存写入 Redis
     */
    public void preheatActivity(Long activityId) {
        SeckillActivity activity = activityService.getById(activityId);
        SeckillStock stock = activityService.getStock(activityId);

        if (stock == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "活动库存信息不存在");
        }

        // 写入库存
        String stockKey = RedisConstants.STOCK_KEY + activityId;
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock.getAvailableStock()));

        // 清空旧的已购买用户集合
        String userSetKey = RedisConstants.USER_SET_KEY + activityId;
        stringRedisTemplate.delete(userSetKey);

        // 写入活动详情缓存（JSON序列化）
        ActivityDetailResponse detail = activityService.getActivityDetail(activityId);
        String activityKey = RedisConstants.ACTIVITY_KEY + activityId;
        stringRedisTemplate.opsForValue().set(activityKey,
                toJsonString(detail), RedisConstants.ACTIVITY_EXPIRE, TimeUnit.SECONDS);

        log.info("活动缓存预热完成: activityId={}, stock={}", activityId, stock.getAvailableStock());
    }

    /**
     * 验证缓存一致性
     * 对比 Redis 库存和 MySQL 库存
     */
    public boolean verifyConsistency(Long activityId) {
        SeckillStock dbStock = activityService.getStock(activityId);
        if (dbStock == null) {
            return false;
        }

        String stockKey = RedisConstants.STOCK_KEY + activityId;
        String redisStockStr = stringRedisTemplate.opsForValue().get(stockKey);

        if (redisStockStr == null) {
            log.warn("缓存一致性校验: Redis 中无库存数据, activityId={}", activityId);
            return false;
        }

        int redisStock = Integer.parseInt(redisStockStr);
        // Redis 库存 = MySQL 可用库存 - 未同步的订单（正常情况下相等）
        boolean consistent = redisStock == dbStock.getAvailableStock();
        if (!consistent) {
            log.warn("缓存不一致: activityId={}, redis={}, mysql={}",
                    activityId, redisStock, dbStock.getAvailableStock());
        }
        return consistent;
    }

    /**
     * 清除活动缓存
     */
    public void clearActivityCache(Long activityId) {
        stringRedisTemplate.delete(RedisConstants.ACTIVITY_KEY + activityId);
        stringRedisTemplate.delete(RedisConstants.STOCK_KEY + activityId);
        stringRedisTemplate.delete(RedisConstants.USER_SET_KEY + activityId);
        log.info("活动缓存已清除: activityId={}", activityId);
    }

    /**
     * Redis 库存回补
     */
    public void restoreRedisStock(Long activityId, int quantity) {
        String stockKey = RedisConstants.STOCK_KEY + activityId;
        stringRedisTemplate.opsForValue().increment(stockKey, quantity);
    }

    /**
     * 移除用户购买标记
     */
    public void removeUserFromSet(Long activityId, Long userId) {
        String userSetKey = RedisConstants.USER_SET_KEY + activityId;
        stringRedisTemplate.opsForSet().remove(userSetKey, String.valueOf(userId));
    }

    private String toJsonString(Object obj) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
}
