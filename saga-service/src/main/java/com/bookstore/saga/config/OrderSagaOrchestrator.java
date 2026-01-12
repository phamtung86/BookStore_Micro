package com.bookstore.saga.config;

import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.common.messaging.order.CancelOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConstants.SAGA_ORDER_QUEUE)
    public void start(Long orderId) {
        rabbitTemplate.convertAndSend(RabbitMQConstants.INVENTORY_EXCHANGE, "inventory.stock", orderId);
    }

    @RabbitListener(queues = RabbitMQConstants.SAGA_INVENTORY_OUT_OFF_STOCK_QUEUE)
    public void outOfStock(CancelOrderMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConstants.ORDER_EXCHANGE, RabbitMQConstants.ORDER_CREATE_FAIL, message);
    }

}
