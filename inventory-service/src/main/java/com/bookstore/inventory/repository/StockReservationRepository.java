package com.bookstore.inventory.repository;

import com.bookstore.inventory.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    Optional<StockReservation> findByOrderId(Long orderId);

    List<StockReservation> findByOrderIdAndStatus(Long orderId, StockReservation.ReservationStatus status);

    List<StockReservation> findByInventoryId(Long inventoryId);

    // Tìm các reservation đã hết hạn
    @Query("SELECT r FROM StockReservation r WHERE r.status = :status AND r.expiresAt < :now")
    List<StockReservation> findExpiredReservations(
            @Param("status") StockReservation.ReservationStatus status,
            @Param("now") LocalDateTime now);

    // Tìm các reservation pending của một order
    @Query("SELECT r FROM StockReservation r WHERE r.orderId = :orderId AND r.status = 'PENDING'")
    List<StockReservation> findPendingByOrderId(@Param("orderId") Long orderId);

    // Đếm số lượng đã reserve cho một product
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM StockReservation r " +
            "WHERE r.inventory.productId = :productId AND r.status = 'PENDING'")
    Integer getTotalReservedByProductId(@Param("productId") Long productId);
}
