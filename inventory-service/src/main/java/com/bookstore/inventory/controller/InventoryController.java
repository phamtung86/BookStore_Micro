package com.bookstore.inventory.controller;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.inventory.dto.StockReservationRequest;
import com.bookstore.inventory.dto.StockReservationResponse;
import com.bookstore.inventory.service.IInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory Management APIs")
public class InventoryController {

    private final IInventoryService inventoryService;

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public ResponseEntity<ServiceResponse> reserveStock(
            @RequestBody StockReservationRequest request) {
        StockReservationResponse response = inventoryService.reserveStock(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS("Stock reserved successfully", response));
        } else {
            return ResponseEntity.badRequest()
                    .body(ServiceResponse.RESPONSE_ERROR("Failed to reserve stock", response));
        }
    }

    @PostMapping("/confirm/{orderId}")
    @Operation(summary = "Confirm stock reservation after successful payment")
    public ResponseEntity<ServiceResponse> confirmReservation(@PathVariable Long orderId) {
        boolean success = inventoryService.confirmReservation(orderId);

        if (success) {
            return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS("Reservation confirmed", null));
        } else {
            return ResponseEntity.badRequest()
                    .body(ServiceResponse.RESPONSE_ERROR("Failed to confirm reservation", null));
        }
    }

    @PostMapping("/release/{orderId}")
    @Operation(summary = "Release stock reservation (order cancelled)")
    public ResponseEntity<ServiceResponse> releaseReservation(@PathVariable Long orderId) {
        boolean success = inventoryService.releaseReservation(orderId);

        if (success) {
            return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS("Reservation released", null));
        } else {
            return ResponseEntity.badRequest()
                    .body(ServiceResponse.RESPONSE_ERROR("Failed to release reservation", null));
        }
    }

    @GetMapping("/available/{productId}")
    @Operation(summary = "Get available stock for a product")
    public ResponseEntity<ServiceResponse> getAvailableStock(@PathVariable Long productId) {
        Integer available = inventoryService.getAvailableStock(productId);
        return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS(available));
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check if stock is available for a product")
    public ResponseEntity<ServiceResponse> checkStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        boolean available = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS(available));
    }
}
