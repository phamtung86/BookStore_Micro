package com.bookstore.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order", columnList = "order_id"),
        @Index(name = "idx_order_items_product", columnList = "product_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_image", length = 500)
    private String productImage;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "returned_quantity")
    @Builder.Default
    private Integer returnedQuantity = 0;

    @Column(name = "refunded_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void calculateSubtotal() {
        BigDecimal gross = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        this.subtotal = gross.subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }

    public boolean isFullyReturned() {
        return returnedQuantity != null && returnedQuantity.equals(quantity);
    }

    public boolean isPartiallyReturned() {
        return returnedQuantity != null && returnedQuantity > 0 && returnedQuantity < quantity;
    }

    public int getReturnableQuantity() {
        return quantity - (returnedQuantity != null ? returnedQuantity : 0);
    }

    public void returnItems(int quantity, BigDecimal refundAmount) {
        this.returnedQuantity = (this.returnedQuantity != null ? this.returnedQuantity : 0) + quantity;
        this.refundedAmount = (this.refundedAmount != null ? this.refundedAmount : BigDecimal.ZERO).add(refundAmount);
    }
}
