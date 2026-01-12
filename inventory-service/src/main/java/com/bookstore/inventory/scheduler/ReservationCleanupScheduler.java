package com.bookstore.inventory.scheduler;

import com.bookstore.inventory.entity.Inventory;
import com.bookstore.inventory.entity.StockReservation;
import com.bookstore.inventory.repository.InventoryRepository;
import com.bookstore.inventory.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupScheduler {

    private final StockReservationRepository stockReservationRepository;
    private final InventoryRepository inventoryRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        List<StockReservation> expiredReservations = stockReservationRepository
                .findExpiredReservations(StockReservation.ReservationStatus.PENDING, LocalDateTime.now());

        if (expiredReservations.isEmpty()) {
            return;
        }

        for (StockReservation reservation : expiredReservations) {
            try {
                Inventory inventory = reservation.getInventory();

                inventory.setReservedQuantity(
                        Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
                inventoryRepository.save(inventory);

                reservation.setStatus(StockReservation.ReservationStatus.EXPIRED);
                stockReservationRepository.save(reservation);

            } catch (Exception e) {
                log.error("Failed to release expired reservation {}: {}",
                        reservation.getId(), e.getMessage());
            }
        }
    }
}
