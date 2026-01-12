package com.bookstore.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInput {
    private String paymentCode;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentGateway;
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
