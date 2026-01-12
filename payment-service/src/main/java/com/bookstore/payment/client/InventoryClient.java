package com.bookstore.payment.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {



}
