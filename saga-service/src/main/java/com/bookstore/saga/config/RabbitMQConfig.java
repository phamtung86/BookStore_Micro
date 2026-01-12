package com.bookstore.saga.config;

import com.bookstore.common.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Queue sagaOrderQueue() {
        return new Queue(RabbitMQConstants.SAGA_ORDER_QUEUE, true);
    }

    @Bean
    public Queue sagaOutOfStockQueue() {
        return new Queue(RabbitMQConstants.SAGA_INVENTORY_OUT_OFF_STOCK_QUEUE, true);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return new Queue(RabbitMQConstants.PROCESS_PAYMENT_SUCCESS_QUEUE, true);
    }

    @Bean
    public Queue paymentFailQueue() {
        return new Queue(RabbitMQConstants.PROCESS_PAYMENT_FAIL_QUEUE, true);
    }
}
