package com.bookstore.media.config;

import com.bookstore.common.messaging.RabbitMQConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public DirectExchange fileExchange() {
        return new DirectExchange(RabbitMQConstants.FILE_EXCHANGE);
    }

    @Bean
    public Queue fileUploadQueue() {
        return QueueBuilder.durable(RabbitMQConstants.FILE_UPLOAD_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.FILE_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue fileUploadResultQueue() {
        return QueueBuilder.durable(RabbitMQConstants.FILE_UPLOAD_RESULT_QUEUE).build();
    }

    @Bean
    public Binding uploadBinding(Queue fileUploadQueue, DirectExchange fileExchange) {
        return BindingBuilder.bind(fileUploadQueue)
                .to(fileExchange)
                .with(RabbitMQConstants.FILE_UPLOAD_ROUTING_KEY);
    }

    @Bean
    public Binding resultBinding(Queue fileUploadResultQueue, DirectExchange fileExchange) {
        return BindingBuilder.bind(fileUploadResultQueue)
                .to(fileExchange)
                .with(RabbitMQConstants.FILE_UPLOAD_RESULT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange fileDeadLetterExchange() {
        return new DirectExchange(RabbitMQConstants.FILE_EXCHANGE + ".dlx");
    }

    @Bean
    public Queue fileUploadDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstants.FILE_UPLOAD_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding fileUploadDeadLetterBinding() {
        return BindingBuilder.bind(fileUploadDeadLetterQueue())
                .to(fileDeadLetterExchange())
                .with(RabbitMQConstants.FILE_UPLOAD_ROUTING_KEY);
    }
}
