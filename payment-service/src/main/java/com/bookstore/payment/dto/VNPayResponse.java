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
public class VNPayResponse {
    private boolean success;
    private String message;
    private String responseCode;
    private String transactionNo;
    private String txnRef;
    private BigDecimal amount;
    private String bankCode;
    private String bankTranNo;
    private String cardType;
    private String payDate;
    private Long orderId;
    private String orderInfo;

    public static VNPayResponse success(String transactionNo, String txnRef, BigDecimal amount) {
        return VNPayResponse.builder()
                .success(true)
                .message("Payment successful")
                .responseCode("00")
                .transactionNo(transactionNo)
                .txnRef(txnRef)
                .amount(amount)
                .build();
    }

    public static VNPayResponse failed(String responseCode, String message) {
        return VNPayResponse.builder()
                .success(false)
                .message(message)
                .responseCode(responseCode)
                .build();
    }
}
