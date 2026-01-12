package com.bookstore.order.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.order.dto.order.OrderRequest;
import org.springframework.data.domain.Pageable;

public interface IOrderService {

    ServiceResponse createOrder(OrderRequest orderRequest);

    ServiceResponse getOrderById(Long id);

    ServiceResponse getOrderByOrderNumber(String orderNumber);

    ServiceResponse getOrdersByUser(Long userId, Pageable pageable);

    ServiceResponse cancelOrder(Long orderId, Long userId, String reason);

    ServiceResponse confirmPayment(Long orderId);
}
