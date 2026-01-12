package com.bookstore.order.service.Impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.messaging.OrderMessage;
import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.order.client.InventoryClient;
import com.bookstore.order.dto.order.OrderDTO;
import com.bookstore.order.dto.order.OrderRequest;
import com.bookstore.order.dto.orderItem.OrderItemDTO;
import com.bookstore.order.dto.orderItem.OrderItemRequest;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public ServiceResponse createOrder(OrderRequest request) {
        BigDecimal subtotal = calculateSubtotal(request.getItems());

        Order order = Order
                .builder()
                .orderNumber(generateOrderNumber())
                .userId(request.getUserId())
                .status(Order.OrderStatus.PENDING)
                .subtotal(subtotal).discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO).totalAmount(subtotal)
                .shippingProvince(request.getShippingProvince())
                .shippingWard(request.getShippingWard())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote())
                .createdAt(LocalDateTime.now())
                .shippingPhone(request.getShippingPhone())
                .shippingRecipientName(request.getShippingRecipientName())
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = OrderItem
                    .builder()
                    .productId(itemReq.getProductId()).productSku(itemReq.getProductSku())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .createdAt(LocalDateTime.now()).productName(itemReq.getProductName())
                    .build();
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        OrderMessage orderMessage = OrderMessage
                .builder().id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId()).status(order.getStatus().toString())
                .currency(order.getCurrency())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstants.INVENTORY_EXCHANGE, "inventory.stock", orderMessage);

        try {
            List<InventoryClient.StockReservationRequest.ReservationItem> reservationItems = request.getItems().stream()
                    .map(item -> InventoryClient.StockReservationRequest.ReservationItem.builder()
                            .productId(item.getProductId()).quantity(item.getQuantity()).build())
                    .collect(Collectors.toList());

            InventoryClient.StockReservationRequest reservationRequest = InventoryClient.StockReservationRequest
                    .builder()
                    .orderId(savedOrder.getId())
                    .userId(savedOrder.getUserId())
                    .items(reservationItems).build();

            ServiceResponse reserveResponse = inventoryClient.reserveStock(reservationRequest);
            Map<String, Object> stockData = (Map<String, Object>) reserveResponse.getData();
            boolean stockSuccess = stockData != null && Boolean.TRUE.equals(stockData.get("success"));
            String stockMessage = stockData != null ? (String) stockData.get("message") : "Unknown error";

            if (!stockSuccess) {
                orderRepository.delete(savedOrder);
                throw new BusinessException("Failed to reserve stock: " + stockMessage);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            orderRepository.delete(savedOrder);
            throw new BusinessException("Failed to reserve stock: " + e.getMessage());
        }
        savedOrder.updateStatus(Order.OrderStatus.CONFIRMED, "Stock reserved", null);
        orderRepository.save(savedOrder);

        return ServiceResponse.RESPONSE_SUCCESS("Order created successfully", mapToDTO(savedOrder));
    }

    private BigDecimal calculateSubtotal(List<OrderItemRequest> items) {
        return items.stream().map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found: " + id));
        return ServiceResponse.RESPONSE_SUCCESS(mapToDTO(order));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException("Order not found: " + orderNumber));
        return ServiceResponse.RESPONSE_SUCCESS(mapToDTO(order));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getOrdersByUser(Long userId, Pageable pageable) {
        Page<OrderDTO> orders = orderRepository.findByUserId(userId, pageable).map(this::mapToDTO);
        return ServiceResponse.RESPONSE_SUCCESS(orders);
    }

    @Override
    @Transactional
    public ServiceResponse cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("You don't have permission to cancel this order");
        }

        if (!order.isCancellable()) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        try {
            inventoryClient.releaseReservation(orderId);
        } catch (Exception e) {
            log.error("Failed to release stock reservation for order {}: {}", orderId, e.getMessage());
        }

        order.cancel(reason, userId);
        orderRepository.save(order);

        return ServiceResponse.RESPONSE_SUCCESS("Order cancelled successfully", mapToDTO(order));
    }

    @Override
    @Transactional
    public ServiceResponse confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found: " + orderId));

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new BusinessException("Order is already paid");
        }

        try {
            inventoryClient.confirmReservation(orderId);
        } catch (Exception e) {
            log.error("Failed to confirm stock for order {}: {}", orderId, e.getMessage());
            throw new BusinessException("Failed to confirm stock: " + e.getMessage());
        }

        order.markAsPaid();
        order.updateStatus(Order.OrderStatus.PROCESSING, "Payment confirmed", null);
        orderRepository.save(order);

        return ServiceResponse.RESPONSE_SUCCESS("Payment confirmed", mapToDTO(order));
    }

    private OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder().id(item.getId()).productId(item.getProductId())
                        .productTitle(item.getProductName()).productSku(item.getProductSku())
                        .thumbnailUrl(item.getProductImage()).quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice()).subtotal(item.getSubtotal()).build())
                .collect(Collectors.toList());

        return OrderDTO.builder().id(order.getId()).orderNumber(order.getOrderNumber()).userId(order.getUserId())
                .status(order.getStatus().name())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .recipientName(order.getShippingRecipientName())
                .recipientPhone(order.getShippingPhone())
                .shippingAddress(order.getFullShippingAddress())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .customerNote(order.getCustomerNote())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt()).build();
    }
}
