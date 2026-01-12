package com.bookstore.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayRequest {
    private Long orderId;
    private Long userId;
    private String orderNumber;
    private BigDecimal amount;
    private String orderInfo;
    private String ipAddress;
    private String bankCode;
    private String locale;
}
