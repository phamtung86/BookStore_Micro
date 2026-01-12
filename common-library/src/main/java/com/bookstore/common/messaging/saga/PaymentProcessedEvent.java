package com.bookstore.common.messaging.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event sent by payment service after payment processing attempt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private boolean success;
    private String transactionId;
    private String failureReason;
    private String paymentStatus;
}
