package com.bookstore.inventory.client;

import com.bookstore.common.dto.response.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {
    @PostMapping("/payments/v1/")
    ServiceResponse createPayment(@RequestBody PaymentInput paymentInput);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PaymentInput {
        private String paymentCode;
        private Long orderId;
        private Long userId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String paymentGateway;
        private String status;
        private String gatewayTransactionId;
        private String gatewayResponseCode;
        private String gatewayResponseMessage;
        private String bankCode;
        private String bankTransactionNo;
        private LocalDateTime paidAt;
        private String ipAddress;
        private String userAgent;
        private String notes;
    }

}
