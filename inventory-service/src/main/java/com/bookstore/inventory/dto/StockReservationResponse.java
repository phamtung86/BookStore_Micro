package com.bookstore.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationResponse {
    private boolean success;
    private String message;
    private Long orderId;
    private List<ReservedItem> reservedItems;
    private List<FailedItem> failedItems;
    private LocalDateTime expiresAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservedItem {
        private Long productId;
        private Integer quantity;
        private Long reservationId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedItem {
        private Long productId;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private String reason;
    }
}
