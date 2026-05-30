package com.flashsalex.mq;

import com.flashsalex.config.RabbitMQConfig;
import com.flashsalex.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单 MQ 消费者
 * 手动 ACK，处理失败进入重试或死信队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrder(SeckillMessage seckillMessage, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到秒杀下单消息: requestId={}, userId={}, activityId={}",
                    seckillMessage.getRequestId(), seckillMessage.getUserId(), seckillMessage.getActivityId());

            String orderNo = orderService.createOrder(seckillMessage);

            // 手动 ACK
            channel.basicAck(deliveryTag, false);
            log.info("秒杀下单消息处理完成: requestId={}, orderNo={}", seckillMessage.getRequestId(), orderNo);

        } catch (Exception e) {
            log.error("秒杀下单消息处理失败: requestId={}", seckillMessage.getRequestId(), e);
            try {
                // 拒绝消息，进入死信队列（不重新入队，避免无限重试）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息NACK失败", ex);
            }
        }
    }
}
