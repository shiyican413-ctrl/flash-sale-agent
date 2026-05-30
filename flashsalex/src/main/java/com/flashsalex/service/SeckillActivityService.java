package com.flashsalex.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashsalex.common.BusinessException;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.dto.request.ActivityCreateRequest;
import com.flashsalex.dto.response.ActivityDetailResponse;
import com.flashsalex.entity.Goods;
import com.flashsalex.entity.SeckillActivity;
import com.flashsalex.entity.SeckillStock;
import com.flashsalex.mapper.GoodsMapper;
import com.flashsalex.mapper.SeckillActivityMapper;
import com.flashsalex.mapper.SeckillStockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillActivityService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillStockMapper stockMapper;
    private final GoodsMapper goodsMapper;

    /**
     * 创建秒杀活动
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createActivity(ActivityCreateRequest request) {
        // 校验商品存在
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品不存在");
        }

        // 校验时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间不能早于开始时间");
        }

        // 创建活动（草稿状态）
        SeckillActivity activity = new SeckillActivity();
        activity.setGoodsId(request.getGoodsId());
        activity.setActivityName(request.getActivityName());
        activity.setSeckillPrice(request.getSeckillPrice());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setStatus(0); // 草稿
        activity.setPerUserLimit(request.getPerUserLimit());
        activityMapper.insert(activity);

        // 创建库存记录
        SeckillStock stock = new SeckillStock();
        stock.setActivityId(activity.getId());
        stock.setTotalStock(request.getTotalStock());
        stock.setAvailableStock(request.getTotalStock());
        stock.setLockedStock(0);
        stock.setSoldStock(0);
        stock.setVersion(0);
        stockMapper.insert(stock);

        log.info("秒杀活动创建成功: id={}, name={}", activity.getId(), activity.getActivityName());
        return activity.getId();
    }

    /**
     * 活动上线
     */
    @Transactional(rollbackFor = Exception.class)
    public void onlineActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        if (activity.getStatus() != 0 && activity.getStatus() != 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有草稿或已下线的活动才能上线");
        }

        LocalDateTime now = LocalDateTime.now();
        int newStatus;
        if (now.isBefore(activity.getStartTime())) {
            newStatus = 1; // 待开始
        } else if (now.isAfter(activity.getEndTime())) {
            newStatus = 3; // 已结束
        } else {
            newStatus = 2; // 进行中
        }

        activity.setStatus(newStatus);
        activityMapper.updateById(activity);
        log.info("秒杀活动上线: id={}, status={}", activityId, newStatus);
    }

    /**
     * 活动下线
     */
    public void offlineActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        activity.setStatus(4); // 已下线
        activityMapper.updateById(activity);
        log.info("秒杀活动下线: id={}", activityId);
    }

    /**
     * 获取活动详情
     */
    public ActivityDetailResponse getActivityDetail(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        Goods goods = goodsMapper.selectById(activity.getGoodsId());
        SeckillStock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<SeckillStock>().eq(SeckillStock::getActivityId, activityId));

        return ActivityDetailResponse.builder()
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .goodsId(activity.getGoodsId())
                .goodsName(goods != null ? goods.getGoodsName() : null)
                .seckillPrice(activity.getSeckillPrice())
                .originalPrice(goods != null ? goods.getOriginalPrice() : null)
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .totalStock(stock != null ? stock.getTotalStock() : 0)
                .availableStock(stock != null ? stock.getAvailableStock() : 0)
                .perUserLimit(activity.getPerUserLimit())
                .build();
    }

    /**
     * 活动分页列表
     */
    public Page<SeckillActivity> listActivities(Integer status, int page, int size) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SeckillActivity::getStatus, status);
        }
        wrapper.orderByDesc(SeckillActivity::getId);
        return activityMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取活动实体
     */
    public SeckillActivity getById(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    /**
     * 获取库存信息
     */
    public SeckillStock getStock(Long activityId) {
        return stockMapper.selectOne(
                new LambdaQueryWrapper<SeckillStock>().eq(SeckillStock::getActivityId, activityId));
    }
}
