package com.bookstore.common.messaging.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to release previously reserved stock
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseStockCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private String reservationId;
    private String reason;
}
