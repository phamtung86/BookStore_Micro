package com.bookstore.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupons_code", columnList = "code"),
    @Index(name = "idx_coupons_active", columnList = "is_active, start_date, end_date")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // e.g., SALE50, NEWYEAR2026

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue; // Percentage (0-100) or fixed amount

    // Limits
    @Column(name = "minimum_order_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "maximum_discount_amount", precision = 12, scale = 2)
    private BigDecimal maximumDiscountAmount; // Cap for percentage discounts

    // Usage limits
    @Column(name = "usage_limit")
    private Integer usageLimit; // Total uses allowed (NULL = unlimited)

    @Column(name = "usage_limit_per_user")
    @Builder.Default
    private Integer usageLimitPerUser = 1;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0; // Current usage count

    // Validity
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Targeting
    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to")
    @Builder.Default
    private AppliesTo appliesTo = AppliesTo.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    @Builder.Default
    private UserType userType = UserType.ALL;

    // Relationships
    @ElementCollection
    @CollectionTable(name = "coupon_products", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    @Builder.Default
    private Set<Long> applicableProductIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "coupon_categories", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "category_id")
    @Builder.Default
    private Set<Long> applicableCategoryIds = new HashSet<>();

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<CouponUsage> usages = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DiscountType {
        PERCENTAGE,     // Giảm theo %
        FIXED_AMOUNT,   // Giảm số tiền cố định
        FREE_SHIPPING   // Miễn phí vận chuyển
    }

    public enum AppliesTo {
        ALL,                 // Tất cả sản phẩm
        SPECIFIC_PRODUCTS,   // Sản phẩm cụ thể
        SPECIFIC_CATEGORIES  // Danh mục cụ thể
    }

    public enum UserType {
        ALL,           // Tất cả users
        NEW_USER,      // User mới
        VIP,           // User VIP
        SPECIFIC_USERS // Users cụ thể
    }

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
               now.isAfter(startDate) &&
               now.isBefore(endDate) &&
               (usageLimit == null || usageCount < usageLimit);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean isNotStarted() {
        return LocalDateTime.now().isBefore(startDate);
    }

    public boolean hasReachedUsageLimit() {
        return usageLimit != null && usageCount >= usageLimit;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid()) return BigDecimal.ZERO;
        if (orderAmount.compareTo(minimumOrderAmount) < 0) return BigDecimal.ZERO;

        BigDecimal discount;
        switch (discountType) {
            case PERCENTAGE:
                discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
                if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
                    discount = maximumDiscountAmount;
                }
                break;
            case FIXED_AMOUNT:
                discount = discountValue;
                break;
            case FREE_SHIPPING:
                discount = BigDecimal.ZERO; // Handled separately
                break;
            default:
                discount = BigDecimal.ZERO;
        }
        return discount.min(orderAmount); // Cannot discount more than order amount
    }

    public boolean isApplicableToProduct(Long productId) {
        if (appliesTo == AppliesTo.ALL) return true;
        if (appliesTo == AppliesTo.SPECIFIC_PRODUCTS) {
            return applicableProductIds.contains(productId);
        }
        return false; // Category check needs to be done in service
    }
}
