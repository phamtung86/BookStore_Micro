package com.bookstore.payment.controller;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.payment.dto.PaymentInput;
import com.bookstore.payment.dto.VNPayRequest;
import com.bookstore.payment.dto.VNPayResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.service.IPaymentService;
import com.bookstore.payment.service.IVNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments/vnpay")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "VNPay", description = "VNPay Payment APIs")
public class VNPayController {

    private final IVNPayService vnPayService;
    private final IPaymentService iPaymentService;

    @PostMapping("/create")
    @Operation(summary = "Create VNPay payment URL")
    public ResponseEntity<ServiceResponse> createPayment(
            @RequestBody VNPayRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        request.setIpAddress(ipAddress);

        PaymentInput paymentInput = PaymentInput.builder()
                .paymentCode(Payment.generatePaymentCode())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency("VND")
                .paymentMethod("VNPAY")
                .paymentGateway("VNPAY")
                .bankCode("NCB")
                .ipAddress(ipAddress)
                .notes("VNPay payment for order: " + request.getOrderNumber())
                .build();

        iPaymentService.createPayment(paymentInput);
        String paymentUrl = vnPayService.createPaymentUrl(request);

        Map<String, String> data = new HashMap<>();
        data.put("paymentUrl", paymentUrl);

        return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS("Payment URL created", data));
    }

    @GetMapping("/callback")
    @Operation(summary = "VNPay callback (Return URL)")
    public ResponseEntity<ServiceResponse> callback(@RequestParam Map<String, String> params) {

        try {
            VNPayResponse response = vnPayService.processCallback(params);

            if (response.isSuccess()) {
                return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS("Payment successful", response));
            } else {
                return ResponseEntity.badRequest()
                        .body(ServiceResponse.RESPONSE_ERROR(response.getMessage(), response));
            }
        } catch (Exception e) {
            log.error("Error in callback: ", e);
            return ResponseEntity.badRequest().body(ServiceResponse.RESPONSE_ERROR(e.getMessage(), null));
        }
    }

    @PostMapping("/ipn")
    @Operation(summary = "VNPay IPN (Instant Payment Notification)")
    public ResponseEntity<String> ipn(@RequestParam Map<String, String> params) {

        try {
            VNPayResponse response = vnPayService.processCallback(params);

            if (response.isSuccess()) {
                return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
            } else {
                return ResponseEntity.ok("{\"RspCode\":\"" + response.getResponseCode() + "\",\"Message\":\""
                        + response.getMessage() + "\"}");
            }
        } catch (Exception e) {
            log.error("Error processing VNPay IPN", e);
            return ResponseEntity.ok("{\"RspCode\":\"99\",\"Message\":\"Unknown error\"}");
        }
    }

    @GetMapping("/query/{txnRef}")
    @Operation(summary = "Query VNPay payment status")
    public ResponseEntity<ServiceResponse> queryStatus(@PathVariable String txnRef) {
        VNPayResponse response = vnPayService.queryPaymentStatus(txnRef);
        return ResponseEntity.ok(ServiceResponse.RESPONSE_SUCCESS("Query result", response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }
}
