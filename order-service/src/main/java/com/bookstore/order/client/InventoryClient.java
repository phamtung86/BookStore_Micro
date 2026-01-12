package com.bookstore.order.client;

import com.bookstore.common.dto.response.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {

    @PostMapping("/inventory/reserve")
    ServiceResponse reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/inventory/confirm/{orderId}")
    ServiceResponse confirmReservation(@PathVariable("orderId") Long orderId);

    @PostMapping("/inventory/release/{orderId}")
    ServiceResponse releaseReservation(@PathVariable("orderId") Long orderId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class StockReservationRequest {
        private Long orderId;
        private List<ReservationItem> items;
        private Long userId;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ReservationItem {
            private Long productId;
            private Integer quantity;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class StockReservationResponse {
        private boolean success;
        private String message;
        private Long orderId;
        private LocalDateTime expiresAt;
    }
}
