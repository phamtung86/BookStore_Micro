package com.bookstore.common.messaging.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event sent by inventory service after stock reservation attempt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private boolean success;
    private String reservationId;
    private String failureReason;
}
