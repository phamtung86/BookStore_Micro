package com.bookstore.order.listener;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.common.messaging.order.CancelOrderMessage;
import com.bookstore.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderListener {

    private final RabbitTemplate rabbitTemplate;
    private final IOrderService iOrderService;

    @RabbitListener(queues = RabbitMQConstants.ORDER_CREATED_FAIL_QUEUE)
    public ResponseEntity<?> orderCreateFail(CancelOrderMessage orderMessage) {
        ServiceResponse response = iOrderService.cancelOrder(orderMessage.getOrderId(), orderMessage.getUserId(),
                orderMessage.getReason());
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }
}
