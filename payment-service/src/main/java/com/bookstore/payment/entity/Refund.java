package com.bookstore.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refunds_code", columnList = "refund_code"),
    @Index(name = "idx_refunds_payment", columnList = "payment_id"),
    @Index(name = "idx_refunds_order", columnList = "order_id"),
    @Index(name = "idx_refunds_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refund_code", nullable = false, unique = true, length = 50)
    private String refundCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @ToString.Exclude
    private Payment payment;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "gateway_refund_id", length = 100)
    private String gatewayRefundId;

    @Column(name = "gateway_response_code", length = 20)
    private String gatewayResponseCode;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RefundStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REJECTED
    }

    public void approve(Long processedBy) {
        this.status = RefundStatus.PROCESSING;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }

    public void complete(String gatewayRefundId, String responseCode) {
        this.status = RefundStatus.COMPLETED;
        this.gatewayRefundId = gatewayRefundId;
        this.gatewayResponseCode = responseCode;
        if (this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public void fail(String responseCode, String notes) {
        this.status = RefundStatus.FAILED;
        this.gatewayResponseCode = responseCode;
        this.notes = notes;
    }

    public void reject(Long processedBy, String reason) {
        this.status = RefundStatus.REJECTED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
        this.notes = reason;
    }

    public static String generateRefundCode() {
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
        String random = String.format("%06d", new java.util.Random().nextInt(999999));
        return "REF-" + timestamp + "-" + random;
    }
}
