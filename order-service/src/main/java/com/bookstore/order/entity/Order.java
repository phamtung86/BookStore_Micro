package com.bookstore.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_number", columnList = "order_number"),
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_payment_status", columnList = "payment_status"),
        @Index(name = "idx_orders_created", columnList = "created_at"),
        @Index(name = "idx_orders_tracking", columnList = "tracking_number")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "shipping_recipient_name", nullable = false, length = 100)
    private String shippingRecipientName;

    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String shippingPhone;

    @Column(name = "shipping_province", nullable = false, length = 100)
    private String shippingProvince;

    @Column(name = "shipping_district", nullable = false, length = 100)
    private String shippingDistrict;

    @Column(name = "shipping_ward", length = 100)
    private String shippingWard;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method")
    @Builder.Default
    private ShippingMethod shippingMethod = ShippingMethod.STANDARD;

    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @OrderBy("createdAt DESC")
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, // Đang chờ xử lý
        CONFIRMED, // Đã xác nhận
        PROCESSING, // Đang xử lý
        SHIPPED, // Đã gửi hàng
        DELIVERED, // Đã giao hàng
        COMPLETED, // Hoàn thành
        CANCELLED, // Đã hủy
        RETURNED, // Trả hàng
        REFUNDED // Hoàn tiền
    }

    public enum ShippingMethod {
        STANDARD, // Giao hàng tiêu chuẩn (3-5 ngày)
        EXPRESS, // Giao hàng nhanh (1-2 ngày)
        SAME_DAY // Giao trong ngày
    }

    public enum PaymentMethod {
        COD, // Thanh toán khi nhận hàng
        BANK_TRANSFER, // Chuyển khoản ngân hàng
        VNPAY, // VNPay
        MOMO, // Ví MoMo
        ZALOPAY, // ZaloPay
        CREDIT_CARD // Thẻ tín dụng
    }

    public enum PaymentStatus {
        PENDING, // Chờ thanh toán
        PAID, // Đã thanh toán
        FAILED, // Thanh toán thất bại
        REFUNDED, // Đã hoàn tiền
        PARTIAL_REFUND // Hoàn tiền một phần
    }

    // Helper methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void addStatusHistory(OrderStatus fromStatus, OrderStatus toStatus, String notes, Long changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .fromStatus(fromStatus != null ? fromStatus.name() : null)
                .toStatus(toStatus.name())
                .notes(notes)
                .changedBy(changedBy)
                .build();
        statusHistory.add(history);
    }

    public void updateStatus(OrderStatus newStatus, String notes, Long changedBy) {
        OrderStatus oldStatus = this.status;
        addStatusHistory(oldStatus, newStatus, notes, changedBy);
        this.status = newStatus;
    }

    public void cancel(String reason, Long cancelledBy) {
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = cancelledBy;
        addStatusHistory(this.status, OrderStatus.CANCELLED, reason, cancelledBy);
    }

    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean isReturnable() {
        return status == OrderStatus.DELIVERED;
    }

    public String getFullShippingAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(shippingAddress);
        if (shippingWard != null)
            sb.append(", ").append(shippingWard);
        sb.append(", ").append(shippingDistrict);
        sb.append(", ").append(shippingProvince);
        return sb.toString();
    }
}
