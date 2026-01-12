package com.bookstore.inventory.service.impl;

import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.common.messaging.order.CancelOrderMessage;
import com.bookstore.inventory.dto.StockReservationRequest;
import com.bookstore.inventory.dto.StockReservationResponse;
import com.bookstore.inventory.entity.Inventory;
import com.bookstore.inventory.entity.StockReservation;
import com.bookstore.inventory.repository.InventoryRepository;
import com.bookstore.inventory.repository.StockReservationRepository;
import com.bookstore.inventory.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements IInventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${inventory.reservation.expiration-minutes:15}")
    private int reservationExpirationMinutes;

    @Override
    @Transactional
    public StockReservationResponse reserveStock(StockReservationRequest request) {
        List<StockReservationResponse.ReservedItem> reservedItems = new ArrayList<>();
        List<StockReservationResponse.FailedItem> failedItems = new ArrayList<>();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(reservationExpirationMinutes);

        for (StockReservationRequest.ReservationItem item : request.getItems()) {
            Inventory inventory = inventoryRepository.findByProductIdWithLock(item.getProductId())
                    .orElse(null);

            if (inventory == null) {
                failedItems.add(StockReservationResponse.FailedItem.builder()
                        .productId(item.getProductId())
                        .requestedQuantity(item.getQuantity())
                        .availableQuantity(0)
                        .reason("Product not found in inventory")
                        .build());
                continue;
            }

            int available = inventory.getQuantity() - inventory.getReorderLevel() - inventory.getReservedQuantity();
            if (available <= item.getQuantity()) {
                CancelOrderMessage cancelOrderMessage = CancelOrderMessage
                        .builder()
                        .orderId(request.getOrderId())
                        .userId(request.getUserId())
                        .reason("PRODUCT OUT OF STOCK")
                        .build();
                rabbitTemplate.convertAndSend(RabbitMQConstants.INVENTORY_EXCHANGE, RabbitMQConstants.INVENTORY_OUT_OF_STOCK_KEY, cancelOrderMessage);
            } else {
                inventory.setReservedQuantity(inventory.getReservedQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);

                StockReservation reservation = StockReservation.builder()
                        .inventory(inventory)
                        .orderId(request.getOrderId())
                        .quantity(item.getQuantity())
                        .status(StockReservation.ReservationStatus.PENDING)
                        .expiresAt(expiresAt)
                        .productId(item.getProductId())
                        .build();
                StockReservation savedReservation = stockReservationRepository.save(reservation);

                reservedItems.add(StockReservationResponse.ReservedItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .reservationId(savedReservation.getId())
                        .build());
            }
        }

        if (!failedItems.isEmpty()) {
            for (StockReservationResponse.ReservedItem reserved : reservedItems) {
                inventoryRepository.releaseReservedStock(reserved.getProductId(), reserved.getQuantity());
            }
            stockReservationRepository.findPendingByOrderId(request.getOrderId())
                    .forEach(r -> {
                        r.setStatus(StockReservation.ReservationStatus.CANCELLED);
                        stockReservationRepository.save(r);
                    });

            return StockReservationResponse.builder()
                    .success(false)
                    .message("Failed to reserve stock for some items")
                    .orderId(request.getOrderId())
                    .reservedItems(new ArrayList<>())
                    .failedItems(failedItems)
                    .build();
        }

        return StockReservationResponse.builder()
                .success(true)
                .message("Stock reserved successfully")
                .orderId(request.getOrderId())
                .reservedItems(reservedItems)
                .failedItems(new ArrayList<>())
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional
    public boolean confirmReservation(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findPendingByOrderId(orderId);

        if (reservations.isEmpty()) {
            log.warn("No pending reservations found for order {}", orderId);
            return false;
        }

        for (StockReservation reservation : reservations) {
            Inventory inventory = reservation.getInventory();

            inventory.setQuantity(inventory.getQuantity() - reservation.getQuantity());
            inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
            inventoryRepository.save(inventory);

            reservation.setStatus(StockReservation.ReservationStatus.CONFIRMED);
            stockReservationRepository.save(reservation);

        }

        return true;
    }

    @Override
    @Transactional
    public boolean releaseReservation(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findPendingByOrderId(orderId);

        if (reservations.isEmpty()) {
            log.warn("No pending reservations found for order {}", orderId);
            return false;
        }

        for (StockReservation reservation : reservations) {
            Inventory inventory = reservation.getInventory();

            inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
            inventoryRepository.save(inventory);

            reservation.setStatus(StockReservation.ReservationStatus.RELEASED);
            stockReservationRepository.save(reservation);

        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableStock(Long productId) {
        return inventoryRepository.getAvailableQuantity(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, Integer quantity) {
        Integer available = inventoryRepository.getAvailableQuantity(productId);
        return available != null && available >= quantity;
    }
}
