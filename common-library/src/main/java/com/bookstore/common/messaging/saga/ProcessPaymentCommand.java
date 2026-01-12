package com.bookstore.common.messaging.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Command to process payment for an order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String orderNumber;
}
