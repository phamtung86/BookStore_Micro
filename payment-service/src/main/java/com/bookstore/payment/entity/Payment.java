package com.bookstore.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_code", columnList = "payment_code"),
    @Index(name = "idx_payments_order", columnList = "order_id"),
    @Index(name = "idx_payments_user", columnList = "user_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_gateway_txn", columnList = "gateway_transaction_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_code", nullable = false, unique = true, length = 50)
    private String paymentCode;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "gateway_response_code", length = 20)
    private String gatewayResponseCode;

    @Column(name = "gateway_response_message", length = 255)
    private String gatewayResponseMessage;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "bank_transaction_no", length = 100)
    private String bankTransactionNo;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Refund> refunds = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        COD,            // Cash on Delivery
        BANK_TRANSFER,  // Bank Transfer
        VNPAY,          // VNPay
        MOMO,           // MoMo Wallet
        ZALOPAY,        // ZaloPay
        CREDIT_CARD     // Credit Card
    }

    public enum PaymentStatus {
        PENDING,        // Waiting for payment
        PROCESSING,     // Payment in progress
        COMPLETED,      // Payment successful
        FAILED,         // Payment failed
        CANCELLED,      // Payment cancelled
        REFUNDED,       // Fully refunded
        PARTIAL_REFUND  // Partially refunded
    }

    public void markAsCompleted(String gatewayTransactionId, String responseCode, String responseMessage) {
        this.status = PaymentStatus.COMPLETED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponseCode = responseCode;
        this.gatewayResponseMessage = responseMessage;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed(String responseCode, String responseMessage) {
        this.status = PaymentStatus.FAILED;
        this.gatewayResponseCode = responseCode;
        this.gatewayResponseMessage = responseMessage;
        this.failedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public BigDecimal getTotalRefundedAmount() {
        return refunds.stream()
            .filter(r -> r.getStatus() == Refund.RefundStatus.COMPLETED)
            .map(Refund::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRefundableAmount() {
        return this.amount.subtract(getTotalRefundedAmount());
    }

    public boolean isRefundable() {
        return status == PaymentStatus.COMPLETED && getRefundableAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCOD() {
        return paymentMethod == PaymentMethod.COD;
    }

    public boolean requiresOnlinePayment() {
        return paymentMethod != PaymentMethod.COD && paymentMethod != PaymentMethod.BANK_TRANSFER;
    }

    public static String generatePaymentCode() {
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
        String random = String.format("%06d", new java.util.Random().nextInt(999999));
        return "PAY-" + timestamp + "-" + random;
    }
}
