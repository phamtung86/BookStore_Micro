package com.bookstore.product.client;

import com.bookstore.common.dto.response.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.Set;

@FeignClient(name = "IDENTITY-SERVICE")
public interface UserClient {
    @GetMapping("/users/{id}")
    ServiceResponse getUserById(@PathVariable Long id);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private UserRole role;
        private boolean enabled;
        private String phoneNumber;
        private String address;
        private LocalDateTime createdAt;
        private Set<String> permissions;
    }

    enum UserRole {
        ADMIN, SELLER, CUSTOMER
    }

}
