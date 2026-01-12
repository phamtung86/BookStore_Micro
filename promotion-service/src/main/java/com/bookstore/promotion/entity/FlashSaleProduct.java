package com.bookstore.promotion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_products", indexes = {
    @Index(name = "idx_flash_sale_products", columnList = "flash_sale_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_flash_product", columnNames = {"flash_sale_id", "product_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    @ToString.Exclude
    private FlashSale flashSale;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "flash_sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal flashSalePrice;

    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "quantity_limit", nullable = false)
    private Integer quantityLimit;

    @Column(name = "quantity_sold")
    @Builder.Default
    private Integer quantitySold = 0;

    @Column(name = "per_user_limit")
    @Builder.Default
    private Integer perUserLimit = 1;

    public int getAvailableQuantity() {
        return quantityLimit - quantitySold;
    }

    public boolean isAvailable() {
        return getAvailableQuantity() > 0;
    }

    public boolean isSoldOut() {
        return quantitySold >= quantityLimit;
    }

    public BigDecimal getDiscountAmount() {
        return originalPrice.subtract(flashSalePrice);
    }

    public BigDecimal getDiscountPercent() {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getDiscountAmount()
            .multiply(BigDecimal.valueOf(100))
            .divide(originalPrice, 2, java.math.RoundingMode.HALF_UP);
    }

    public void incrementSold(int quantity) {
        if (quantitySold + quantity > quantityLimit) {
            throw new IllegalStateException("Not enough stock in flash sale");
        }
        this.quantitySold += quantity;
    }

    public boolean canPurchase(int requestedQuantity) {
        return requestedQuantity <= getAvailableQuantity() && requestedQuantity <= perUserLimit;
    }
}
