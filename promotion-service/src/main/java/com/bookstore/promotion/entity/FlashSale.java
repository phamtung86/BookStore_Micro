package com.bookstore.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flash_sales", indexes = {
    @Index(name = "idx_flash_sales_time", columnList = "start_time, end_time"),
    @Index(name = "idx_flash_sales_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FlashSaleStatus status = FlashSaleStatus.UPCOMING;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<FlashSaleProduct> products = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FlashSaleStatus {
        UPCOMING,  // Sắp diễn ra
        ACTIVE,    // Đang diễn ra
        ENDED      // Đã kết thúc
    }

    public void addProduct(FlashSaleProduct product) {
        products.add(product);
        product.setFlashSale(this);
    }

    public void removeProduct(FlashSaleProduct product) {
        products.remove(product);
        product.setFlashSale(null);
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               status == FlashSaleStatus.ACTIVE &&
               now.isAfter(startTime) && 
               now.isBefore(endTime);
    }

    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startTime);
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            this.status = FlashSaleStatus.UPCOMING;
        } else if (now.isAfter(endTime)) {
            this.status = FlashSaleStatus.ENDED;
        } else {
            this.status = FlashSaleStatus.ACTIVE;
        }
    }

    public long getRemainingSeconds() {
        if (status != FlashSaleStatus.ACTIVE) return 0;
        return java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
    }
}
