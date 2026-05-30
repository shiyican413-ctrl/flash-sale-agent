package com.flashsalex.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashsalex.common.BusinessException;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.common.RedisConstants;
import com.flashsalex.config.RabbitMQConfig;
import com.flashsalex.mq.SeckillMessage;
import com.flashsalex.entity.*;
import com.flashsalex.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final SeckillActivityMapper activityMapper;
    private final SeckillStockMapper stockMapper;
    private final StockFlowMapper stockFlowMapper;
    private final MessageLogMapper messageLogMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    /**
     * MQ 消费者调用：异步创建订单
     * 包含消息幂等、唯一索引兜底
     */
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(SeckillMessage message) {
        String messageId = message.getRequestId();

        // 1. 消息幂等检查
        MessageLog existLog = messageLogMapper.selectOne(
                new LambdaQueryWrapper<MessageLog>().eq(MessageLog::getMessageId, messageId));
        if (existLog != null && "SUCCESS".equals(existLog.getStatus())) {
            log.info("消息已处理，跳过: messageId={}", messageId);
            return existLog.getBusinessKey(); // 返回已有的 orderNo
        }

        // 记录消息处理状态
        if (existLog == null) {
            MessageLog msgLog = new MessageLog();
            msgLog.setMessageId(messageId);
            msgLog.setBusinessType("SECKILL_ORDER");
            msgLog.setBusinessKey(message.getActivityId() + ":" + message.getUserId());
            msgLog.setStatus("PROCESSING");
            msgLog.setRetryCount(0);
            messageLogMapper.insert(msgLog);
        }

        try {
            // 2. 查询活动和商品
            SeckillActivity activity = activityMapper.selectById(message.getActivityId());
            if (activity == null) {
                throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
            }

            // 3. 生成订单号
            String orderNo = generateOrderNo();

            // 4. 数据库扣减库存（乐观锁兜底）
            int deductRows = stockMapper.deductStock(message.getActivityId(), message.getQuantity());
            if (deductRows == 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "数据库库存扣减失败");
            }

            // 5. 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setRequestId(messageId);
            order.setUserId(message.getUserId());
            order.setActivityId(message.getActivityId());
            order.setGoodsId(message.getGoodsId());
            order.setQuantity(message.getQuantity());
            order.setPayAmount(activity.getSeckillPrice().multiply(new BigDecimal(message.getQuantity())));
            order.setStatus("WAIT_PAY");
            order.setExpireTime(LocalDateTime.now().plusMinutes(15));
            orderMapper.insert(order);

            // 6. 记录库存流水
            SeckillStock stock = stockMapper.selectOne(
                    new LambdaQueryWrapper<SeckillStock>().eq(SeckillStock::getActivityId, message.getActivityId()));
            StockFlow flow = new StockFlow();
            flow.setFlowNo(IdUtil.fastSimpleUUID());
            flow.setActivityId(message.getActivityId());
            flow.setOrderNo(orderNo);
            flow.setUserId(message.getUserId());
            flow.setChangeType("ORDER_CREATED");
            flow.setChangeQuantity(message.getQuantity());
            if (stock != null) {
                flow.setBeforeStock(stock.getAvailableStock() + message.getQuantity());
                flow.setAfterStock(stock.getAvailableStock());
            }
            flow.setRemark("秒杀订单创建");
            stockFlowMapper.insert(flow);

            // 7. 发送订单超时消息
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_TIMEOUT_ROUTING_KEY,
                    orderNo);

            // 8. 更新 Redis 秒杀结果
            String resultKey = RedisConstants.RESULT_KEY + message.getActivityId() + ":" + message.getUserId();
            stringRedisTemplate.opsForValue().set(resultKey, "SUCCESS:" + orderNo,
                    RedisConstants.RESULT_EXPIRE, TimeUnit.SECONDS);

            // 9. 更新消息状态
            updateMessageLog(messageId, "SUCCESS", null);

            log.info("秒杀订单创建成功: orderNo={}, userId={}, activityId={}",
                    orderNo, message.getUserId(), message.getActivityId());
            return orderNo;

        } catch (BusinessException e) {
            // 订单创建失败，回补 Redis
            rollbackRedis(message.getActivityId(), message.getUserId());
            updateMessageLog(messageId, "FAILED", e.getMessage());

            // 更新 Redis 结果为失败
            String resultKey = RedisConstants.RESULT_KEY + message.getActivityId() + ":" + message.getUserId();
            stringRedisTemplate.opsForValue().set(resultKey, "FAILED",
                    RedisConstants.RESULT_EXPIRE, TimeUnit.SECONDS);

            throw e;
        }
    }

    /**
     * 查询用户订单列表
     */
    public Page<Order> listOrders(Long userId, int page, int size) {
        return orderMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt));
    }

    /**
     * 查询订单详情
     */
    public Order getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 更新消息日志状态
     */
    private void updateMessageLog(String messageId, String status, String error) {
        MessageLog msgLog = messageLogMapper.selectOne(
                new LambdaQueryWrapper<MessageLog>().eq(MessageLog::getMessageId, messageId));
        if (msgLog != null) {
            msgLog.setStatus(status);
            if (error != null) {
                msgLog.setErrorMessage(error);
            }
            msgLog.setRetryCount(msgLog.getRetryCount() + 1);
            messageLogMapper.updateById(msgLog);
        }
    }

    /**
     * 回滚 Redis 库存
     */
    private void rollbackRedis(Long activityId, Long userId) {
        stringRedisTemplate.opsForValue().increment(RedisConstants.STOCK_KEY + activityId);
        stringRedisTemplate.opsForSet().remove(RedisConstants.USER_SET_KEY + activityId, String.valueOf(userId));
        log.warn("订单创建失败，Redis回滚: activityId={}, userId={}", activityId, userId);
    }

    /**
     * 生成订单号: SO + 日期 + 序列
     */
    private String generateOrderNo() {
        return "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
