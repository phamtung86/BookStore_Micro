package com.bookstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_reservations", indexes = {
        @Index(name = "idx_reservations_order", columnList = "order_id"),
        @Index(name = "idx_reservations_status", columnList = "status"),
        @Index(name = "idx_reservations_expires", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @ToString.Exclude
    private Inventory inventory;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReservationStatus {
        PENDING, // Đang giữ hàng
        CONFIRMED, // Đã xác nhận (thanh toán thành công)
        RELEASED, // Đã giải phóng (hết hạn hoặc hủy)
        EXPIRED, CANCELLED // Đã hủy bởi user
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }

    public boolean canBeReleased() {
        return status == ReservationStatus.PENDING && isExpired();
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending reservations");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can only release pending reservations");
        }
        this.status = ReservationStatus.RELEASED;
    }

    public void cancel() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending reservations");
        }
        this.status = ReservationStatus.CANCELLED;
    }
}
