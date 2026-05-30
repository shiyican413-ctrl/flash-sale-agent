package com.flashsalex.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.flashsalex.common.BusinessException;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.common.RedisConstants;
import com.flashsalex.config.RabbitMQConfig;
import com.flashsalex.mq.SeckillMessage;
import com.flashsalex.dto.request.SeckillRequest;
import com.flashsalex.dto.response.SeckillResultResponse;
import com.flashsalex.entity.SeckillActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀核心服务
 * 处理验证码、动态地址、Lua 扣库存、MQ 发消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillActivityService activityService;
    private final RabbitTemplate rabbitTemplate;

    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setResultType(Long.class);
        seckillScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua/seckill.lua")));
    }

    /**
     * 获取验证码
     * 返回 captchaId 和验证码答案
     */
    public String getCaptcha(Long activityId, Long userId) {
        // 生成算术验证码，如 "3+5"
        int a = RandomUtil.randomInt(1, 10);
        int b = RandomUtil.randomInt(1, 10);
        String captchaId = IdUtil.fastSimpleUUID();
        String answer = String.valueOf(a + b);

        // 存储验证码答案到 Redis
        String captchaKey = RedisConstants.CAPTCHA_KEY + activityId + ":" + userId;
        stringRedisTemplate.opsForValue().set(captchaKey, answer,
                RedisConstants.CAPTCHA_EXPIRE, TimeUnit.SECONDS);

        // 返回验证码问题（实际项目中应返回图片 base64，这里简化返回算术题）
        String captchaQuestion = a + "+" + b;
        log.debug("验证码生成: activityId={}, userId={}, question={}, answer={}",
                activityId, userId, captchaQuestion, answer);
        return captchaId + ":" + captchaQuestion;
    }

    /**
     * 验证验证码并返回动态秒杀地址
     */
    public String getSeckillPath(Long activityId, Long userId, String captchaId, String captchaCode) {
        // 校验验证码
        String captchaKey = RedisConstants.CAPTCHA_KEY + activityId + ":" + userId;
        String cachedAnswer = stringRedisTemplate.opsForValue().get(captchaKey);
        if (cachedAnswer == null || !cachedAnswer.equals(captchaCode)) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }
        // 验证后删除
        stringRedisTemplate.delete(captchaKey);

        // 校验活动状态
        SeckillActivity activity = activityService.getById(activityId);
        validateActivity(activity);

        // 生成动态地址
        String path = IdUtil.fastSimpleUUID().substring(0, 16);
        String pathKey = RedisConstants.PATH_KEY + activityId + ":" + userId;
        stringRedisTemplate.opsForValue().set(pathKey, path,
                RedisConstants.PATH_EXPIRE, TimeUnit.SECONDS);

        log.info("动态秒杀地址生成: activityId={}, userId={}", activityId, userId);
        return path;
    }

    /**
     * 执行秒杀
     * 1. 校验动态地址
     * 2. Redis Lua 原子扣库存
     * 3. 发送 MQ 消息
     */
    public String executeSeckill(Long activityId, Long userId, String path, SeckillRequest request) {
        // 校验动态地址
        String pathKey = RedisConstants.PATH_KEY + activityId + ":" + userId;
        String cachedPath = stringRedisTemplate.opsForValue().get(pathKey);
        if (cachedPath == null || !cachedPath.equals(path)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "秒杀地址无效或已过期");
        }
        stringRedisTemplate.delete(pathKey);

        // 校验活动状态
        SeckillActivity activity = activityService.getById(activityId);
        validateActivity(activity);

        // Lua 脚本原子扣库存
        String stockKey = RedisConstants.STOCK_KEY + activityId;
        String userSetKey = RedisConstants.USER_SET_KEY + activityId;

        Long result = stringRedisTemplate.execute(
                seckillScript,
                java.util.List.of(stockKey, userSetKey),
                String.valueOf(userId)
        );

        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "秒杀脚本执行异常");
        }

        if (result == 1L) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        if (result == 2L) {
            throw new BusinessException(ErrorCode.ALREADY_PURCHASED);
        }

        // 扣减成功，发送 MQ 消息
        String requestId = IdUtil.fastSimpleUUID();
        SeckillMessage message = SeckillMessage.builder()
                .requestId(requestId)
                .userId(userId)
                .activityId(activityId)
                .goodsId(activity.getGoodsId())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .createTime(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SECKILL_EXCHANGE,
                    RabbitMQConfig.SECKILL_ORDER_ROUTING_KEY,
                    message);
        } catch (Exception e) {
            // MQ 发送失败，回滚 Redis
            rollbackRedis(activityId, userId);
            log.error("秒杀消息发送失败: activityId={}, userId={}", activityId, userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }

        // 设置秒杀结果为处理中
        String resultKey = RedisConstants.RESULT_KEY + activityId + ":" + userId;
        stringRedisTemplate.opsForValue().set(resultKey, "PROCESSING:" + requestId,
                RedisConstants.RESULT_EXPIRE, TimeUnit.SECONDS);

        log.info("秒杀请求处理完成: activityId={}, userId={}, requestId={}", activityId, userId, requestId);
        return requestId;
    }

    /**
     * 查询秒杀结果
     */
    public SeckillResultResponse getSeckillResult(String requestId, Long userId) {
        // 遍历可能的活动（简化：通过 requestId 前缀查找，实际可优化）
        // 这里通过结果缓存查询
        // 消费者处理完会更新结果，格式: SUCCESS:{orderNo} 或 FAILED
        // 由于需要 activityId 来拼 key，这里简化为直接查询 MQ 消费状态

        // 返回 PROCESSING 状态，前端轮询
        return SeckillResultResponse.builder()
                .requestId(requestId)
                .status("PROCESSING")
                .orderNo(null)
                .build();
    }

    /**
     * 查询指定活动的秒杀结果
     */
    public SeckillResultResponse getSeckillResult(Long activityId, Long userId) {
        String resultKey = RedisConstants.RESULT_KEY + activityId + ":" + userId;
        String result = stringRedisTemplate.opsForValue().get(resultKey);

        if (result == null) {
            return SeckillResultResponse.builder()
                    .requestId(null)
                    .status("FAILED")
                    .orderNo(null)
                    .build();
        }

        String[] parts = result.split(":", 2);
        String status = parts[0];
        String orderNo = parts.length > 1 && !"PROCESSING".equals(status) ? parts[1] : null;

        return SeckillResultResponse.builder()
                .requestId(null)
                .status(status)
                .orderNo(orderNo)
                .build();
    }

    /**
     * 校验活动状态
     */
    private void validateActivity(SeckillActivity activity) {
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_STARTED);
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.ACTIVITY_ENDED);
        }
        if (activity.getStatus() != 2) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_STARTED, "活动未在进行中状态");
        }
    }

    /**
     * 回滚 Redis 库存和用户标记
     */
    private void rollbackRedis(Long activityId, Long userId) {
        String stockKey = RedisConstants.STOCK_KEY + activityId;
        String userSetKey = RedisConstants.USER_SET_KEY + activityId;
        stringRedisTemplate.opsForValue().increment(stockKey);
        stringRedisTemplate.opsForSet().remove(userSetKey, String.valueOf(userId));
        log.warn("Redis 库存回滚: activityId={}, userId={}", activityId, userId);
    }
}
