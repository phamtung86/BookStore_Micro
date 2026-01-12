package com.bookstore.payment.client;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.messaging.order.CancelOrderMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {

    @GetMapping("/orders/{id}")
    ResponseEntity<ServiceResponse> findOrderById(@PathVariable(name = "id") Long id);

    @PostMapping("/cancel")
    ResponseEntity<?> cancelOrder(@RequestBody CancelOrderMessage orderMessage);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class OrderDTO {
        private Long id;
        private String orderNumber;
        private Long userId;
        private String status;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String couponCode;
        private String recipientName;
        private String recipientPhone;
        private String shippingAddress;
        private String paymentMethod;
        private String paymentStatus;
        private String customerNote;
        private List<OrderItemDTO> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productTitle;
        private String productSku;
        private String thumbnailUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

}
