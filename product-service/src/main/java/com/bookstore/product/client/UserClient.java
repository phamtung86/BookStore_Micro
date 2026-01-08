package com.bookstore.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "IDENTITY-SERVICE")
public interface UserClient {
    @GetMapping("/users/{id}")
    com.bookstore.identity.dto.UserDTO getUserById(@PathVariable Long id);

}
