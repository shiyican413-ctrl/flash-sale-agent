package com.flashsalex.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashsalex.common.BusinessException;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.entity.Order;
import com.flashsalex.entity.SeckillStock;
import com.flashsalex.entity.StockFlow;
import com.flashsalex.mapper.OrderMapper;
import com.flashsalex.mapper.SeckillStockMapper;
import com.flashsalex.mapper.StockFlowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 支付与订单超时服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderMapper orderMapper;
    private final SeckillStockMapper stockMapper;
    private final StockFlowMapper stockFlowMapper;
    private final CachePreheatService cachePreheatService;

    /**
     * 模拟支付
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo, Long userId) {
        Order order = getAndValidateOrder(orderNo, userId);
        if (!"WAIT_PAY".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单不是待支付状态");
        }
        if (LocalDateTime.now().isAfter(order.getExpireTime())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单已超时");
        }

        // 更新订单状态为已支付
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 记录库存流水
        recordStockFlow(order, "PAY_SUCCESS", "支付成功");

        log.info("订单支付成功: orderNo={}, userId={}", orderNo, userId);
    }

    /**
     * 用户主动取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        Order order = getAndValidateOrder(orderNo, userId);
        if (!"WAIT_PAY".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有待支付订单可取消");
        }

        order.setStatus("CANCELED");
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 回补库存
        restoreStock(order);

        log.info("订单取消成功: orderNo={}", orderNo);
    }

    /**
     * 超时自动关闭订单（由 MQ 消费者或定时任务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean closeTimeoutOrder(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            log.warn("超时关闭订单: 订单不存在, orderNo={}", orderNo);
            return false;
        }
        if (!"WAIT_PAY".equals(order.getStatus())) {
            log.debug("超时关闭订单: 订单状态不是待支付, orderNo={}, status={}", orderNo, order.getStatus());
            return false;
        }

        order.setStatus("CANCELED");
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 回补库存
        restoreStock(order);

        log.info("超时订单关闭成功: orderNo={}, userId={}", orderNo, order.getUserId());
        return true;
    }

    /**
     * 回补库存（数据库 + Redis）
     */
    private void restoreStock(Order order) {
        // 数据库回补
        int rows = stockMapper.restoreStock(order.getActivityId(), order.getQuantity());
        if (rows == 0) {
            log.error("数据库库存回补失败: activityId={}", order.getActivityId());
        }

        // Redis 回补
        String redisStockKey = "seckill:stock:" + order.getActivityId();
        cachePreheatService.restoreRedisStock(order.getActivityId(), order.getQuantity());

        // 移除用户购买标记（允许重新购买）
        String redisUserSetKey = "seckill:user:set:" + order.getActivityId();
        // 使用 StringRedisTemplate 直接操作（通过 cachePreheatService 封装）
        cachePreheatService.removeUserFromSet(order.getActivityId(), order.getUserId());

        // 记录库存流水
        recordStockFlow(order, "CANCEL_RESTORE", "订单取消/超时，库存回补");

        log.info("库存回补完成: activityId={}, quantity={}", order.getActivityId(), order.getQuantity());
    }

    /**
     * 记录库存流水
     */
    private void recordStockFlow(Order order, String changeType, String remark) {
        SeckillStock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<SeckillStock>().eq(SeckillStock::getActivityId, order.getActivityId()));

        StockFlow flow = new StockFlow();
        flow.setFlowNo(IdUtil.fastSimpleUUID());
        flow.setActivityId(order.getActivityId());
        flow.setOrderNo(order.getOrderNo());
        flow.setUserId(order.getUserId());
        flow.setChangeType(changeType);
        flow.setChangeQuantity(order.getQuantity());
        if (stock != null) {
            if ("CANCEL_RESTORE".equals(changeType)) {
                flow.setBeforeStock(stock.getAvailableStock() - order.getQuantity());
                flow.setAfterStock(stock.getAvailableStock());
            } else {
                flow.setBeforeStock(stock.getAvailableStock());
                flow.setAfterStock(stock.getAvailableStock());
            }
        }
        flow.setRemark(remark);
        stockFlowMapper.insert(flow);
    }

    private Order getAndValidateOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        return order;
    }
}
