package com.bookstore.identity.controller;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.identity.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User Management APIs")
public class UserController {

    private final IUserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ALL_USERS')")
    @Operation(summary = "Get all users - Requires VIEW_ALL_USERS permission")
    public ResponseEntity<ServiceResponse> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @Operation(summary = "Get user by ID - Requires VIEW_USER permission")
    public ResponseEntity<ServiceResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @Operation(summary = "Delete user - Requires DELETE_USER permission")
    public ResponseEntity<ServiceResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ServiceResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }
}
