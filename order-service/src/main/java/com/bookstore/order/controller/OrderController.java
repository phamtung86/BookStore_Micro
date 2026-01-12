package com.bookstore.order.controller;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.messaging.order.CancelOrderMessage;
import com.bookstore.order.dto.order.OrderRequest;
import com.bookstore.order.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "Order Management APIs")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new order", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ServiceResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        request.setUserId(userId);
        ServiceResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getOrderById(@PathVariable(name = "id") Long id) {
        ServiceResponse response = orderService.getOrderById(id);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatusCode()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> orderCreateFail(@RequestBody CancelOrderMessage orderMessage) {
        ServiceResponse response = orderService.cancelOrder(orderMessage.getOrderId(), orderMessage.getUserId(),
                orderMessage.getReason());
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }
}
