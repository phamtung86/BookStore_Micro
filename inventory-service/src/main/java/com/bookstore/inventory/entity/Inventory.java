package com.bookstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_sku", columnList = "sku")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_warehouse", columnNames = { "product_id", "warehouse_id" })
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @ToString.Exclude
    private Warehouse warehouse;

    @Column(nullable = false, length = 50)
    private String sku; 

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;
    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(name = "reorder_quantity")
    @Builder.Default
    private Integer reorderQuantity = 50;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<InventoryMovement> movements = new ArrayList<>();

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<StockReservation> reservations = new ArrayList<>();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public boolean isLowStock() {
        return getAvailableQuantity() <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }

    public boolean hasAvailableStock(int requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }

    public void reserve(int quantity) {
        if (!hasAvailableStock(quantity)) {
            throw new IllegalStateException(
                    "Not enough stock available. Available: " + getAvailableQuantity() + ", Requested: " + quantity);
        }
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    public void addStock(int quantity) {
        this.quantity += quantity;
    }

    public void removeStock(int quantity) {
        if (this.quantity < quantity) {
            throw new IllegalStateException(
                    "Cannot remove more stock than available. Current: " + this.quantity + ", Requested: " + quantity);
        }
        this.quantity -= quantity;
    }

    public void confirmReservation(int quantity) {
        this.reservedQuantity -= quantity;
        this.quantity -= quantity;
    }
}
