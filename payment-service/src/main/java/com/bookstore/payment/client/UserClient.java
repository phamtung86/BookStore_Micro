package com.bookstore.payment.client;

import com.bookstore.common.dto.response.ServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "IDENTITY-SERVICE")
public interface UserClient {
    @GetMapping("/users/{id}")
    ServiceResponse getUserById(@PathVariable Long id);

}
