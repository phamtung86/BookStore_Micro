package com.bookstore.common.messaging.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event for saga compensation results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaCompensationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private String compensatedStep;
    private boolean success;
    private String failureReason;
}
