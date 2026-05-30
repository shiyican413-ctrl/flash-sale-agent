package com.flashsalex.mq;

import com.flashsalex.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时关闭消费者
 * 监听延迟队列过期的消息，关闭超时未支付订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = "order.timeout.close.queue")
    public void handleTimeoutOrder(String orderNo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到订单超时消息: orderNo={}", orderNo);
            boolean closed = paymentService.closeTimeoutOrder(orderNo);
            channel.basicAck(deliveryTag, false);
            if (closed) {
                log.info("超时订单已关闭: orderNo={}", orderNo);
            }
        } catch (Exception e) {
            log.error("超时订单处理失败: orderNo={}", orderNo, e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息NACK失败", ex);
            }
        }
    }
}
