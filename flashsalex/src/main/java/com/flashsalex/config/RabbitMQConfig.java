package com.flashsalex.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== 交换机 =====
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String DLX_EXCHANGE = "seckill.dlx.exchange";

    // ===== 队列 =====
    public static final String SECKILL_ORDER_QUEUE = "seckill.order.queue";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String SECKILL_DLQ = "seckill.dead.queue";
    public static final String ORDER_TIMEOUT_CLOSE_QUEUE = "order.timeout.close.queue";

    // ===== 路由键 =====
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";
    public static final String SECKILL_DLQ_ROUTING_KEY = "seckill.dlq";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ===== 交换机声明 =====
    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ===== 队列声明 =====
    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(SECKILL_ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SECKILL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "order.timeout.close")
                .withArgument("x-message-ttl", 900000) // 15分钟
                .build();
    }

    @Bean
    public Queue seckillDeadQueue() {
        return QueueBuilder.durable(SECKILL_DLQ).build();
    }

    @Bean
    public Queue orderTimeoutCloseQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_CLOSE_QUEUE).build();
    }

    // ===== 绑定 =====
    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder.bind(seckillOrderQueue())
                .to(seckillExchange())
                .with(SECKILL_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderExchange())
                .with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(seckillDeadQueue())
                .to(dlxExchange())
                .with(SECKILL_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutCloseBinding() {
        return BindingBuilder.bind(orderTimeoutCloseQueue())
                .to(orderExchange())
                .with("order.timeout.close");
    }
}
