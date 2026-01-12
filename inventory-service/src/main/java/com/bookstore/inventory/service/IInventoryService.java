package com.bookstore.inventory.service;

import com.bookstore.inventory.dto.StockReservationRequest;
import com.bookstore.inventory.dto.StockReservationResponse;

public interface IInventoryService {

    StockReservationResponse reserveStock(StockReservationRequest request);

    boolean confirmReservation(Long orderId);

    boolean releaseReservation(Long orderId);

    Integer getAvailableStock(Long productId);

    boolean isStockAvailable(Long productId, Integer quantity);
}
