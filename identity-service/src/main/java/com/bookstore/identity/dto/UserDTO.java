package com.bookstore.identity.dto;

import com.bookstore.identity.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
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
