package com.bookstore.order.config;

import com.bookstore.common.messaging.RabbitMQConstants;
import org.springframework.amqp.core.*;
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
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(RabbitMQConstants.ORDER_EXCHANGE);
    }

    @Bean
    public DirectExchange inventoryExchange() {
        return new DirectExchange(RabbitMQConstants.INVENTORY_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(RabbitMQConstants.ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue orderCreateFailQueue(){
        return QueueBuilder.durable(RabbitMQConstants.ORDER_CREATED_FAIL_QUEUE).build();
    }
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with("order.created");
    }
    @Bean
    public Binding orderCreateFailBinding(Queue orderCreateFailQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(orderCreateFailQueue)
                .to(orderExchange)
                .with(RabbitMQConstants.ORDER_CREATE_FAIL);
    }
}
