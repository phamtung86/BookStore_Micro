package com.bookstore.inventory.listener;

import com.bookstore.common.messaging.OrderMessage;
import com.bookstore.common.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConstants.INVENTORY_STOCK_QUEUE)
    public void test(OrderMessage orderMessage) {

    }

}
