package com.bookstore.payment.service.Impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.payment.client.OrderClient;
import com.bookstore.payment.client.UserClient;
import com.bookstore.payment.dto.PaymentInput;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.repository.PaymentRepository;
import com.bookstore.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final UserClient userClient;

    @Transactional
    @Override
    public ServiceResponse createPayment(PaymentInput paymentInput) {

        ServiceResponse orderResponse = orderClient.findOrderById(paymentInput.getOrderId()).getBody();
        if (orderResponse == null || orderResponse.getData() == null) {
            throw new BusinessException("Order not found");
        }

        Map<String, Object> orderData = (Map<String, Object>) orderResponse.getData();
        String orderStatus = (String) orderData.get("status");
        String paymentStatus = (String) orderData.get("paymentStatus");
        if ("CANCELLED".equalsIgnoreCase(orderStatus) && !"PENDING".equalsIgnoreCase(paymentStatus)) {
            throw new BusinessException("Cannot create payment for order with status: " + orderStatus);
        }

        ServiceResponse userResponse = userClient.getUserById(paymentInput.getUserId());
        if (userResponse == null || userResponse.getData() == null) {
            throw new BusinessException("User not found");
        }

        Payment payment = Payment.builder()
                .id(null)
                .paymentCode(paymentInput.getPaymentCode())
                .orderId(paymentInput.getOrderId())
                .userId(paymentInput.getUserId())
                .amount(paymentInput.getAmount())
                .currency(paymentInput.getCurrency())
                .paymentMethod(Payment.PaymentMethod.valueOf((paymentInput.getPaymentMethod().toUpperCase())))
                .paymentGateway(paymentInput.getPaymentGateway())
                .status(Payment.PaymentStatus.PENDING)
                .gatewayResponseCode(paymentInput.getGatewayResponseCode())
                .gatewayResponseMessage(paymentInput.getGatewayResponseMessage())
                .bankCode(paymentInput.getBankCode())
                .bankTransactionNo(paymentInput.getBankTransactionNo())
                .paidAt(paymentInput.getPaidAt())
                .ipAddress(paymentInput.getIpAddress())
                .userAgent(paymentInput.getUserAgent())
                .notes(paymentInput.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        Payment p = paymentRepository.save(payment);
        return ServiceResponse.RESPONSE_SUCCESS(p);
    }
}
