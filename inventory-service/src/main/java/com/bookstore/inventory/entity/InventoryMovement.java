package com.bookstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements", indexes = {
        @Index(name = "idx_movements_product", columnList = "product_id"),
        @Index(name = "idx_movements_type", columnList = "movement_type"),
        @Index(name = "idx_movements_date", columnList = "created_at"),
        @Index(name = "idx_movements_reference", columnList = "reference_type, reference_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @ToString.Exclude
    private Inventory inventory;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @ToString.Exclude
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // ORDER, PURCHASE, ADJUSTMENT, TRANSFER

    @Column(name = "reference_id", length = 50)
    private String referenceId; // Order ID, PO number,

    @Column(length = 255)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MovementType {
        STOCK_IN, // Nhập hàng từ nhà cung cấp
        STOCK_OUT, // Xuất hàng (bán)
        RESERVED, // Đặt trước cho đơn hàng pending
        RELEASED, // Giải phóng khi hủy đơn
        ADJUSTMENT, // Điều chỉnh (kiểm kê)
        TRANSFER_IN, // Chuyển kho vào
        TRANSFER_OUT, // Chuyển kho ra
        RETURN, // Trả hàng từ khách
        DAMAGED // Hàng hỏng/mất
    }

    public static InventoryMovement create(
            Inventory inventory,
            MovementType type,
            int quantity,
            int quantityBefore,
            String referenceType,
            String referenceId,
            String reason,
            Long createdBy) {

        return InventoryMovement.builder()
                .inventory(inventory)
                .productId(inventory.getProductId())
                .warehouse(inventory.getWarehouse())
                .movementType(type)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityBefore + quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .reason(reason)
                .createdBy(createdBy)
                .build();
    }
}
