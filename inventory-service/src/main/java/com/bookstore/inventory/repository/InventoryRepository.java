package com.bookstore.inventory.repository;

import com.bookstore.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);


    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity " +
            "WHERE i.productId = :productId AND i.reservedQuantity >= :quantity")
    int releaseReservedStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT (i.quantity - i.reservedQuantity) FROM Inventory i WHERE i.productId = :productId")
    Integer getAvailableQuantity(@Param("productId") Long productId);

}
