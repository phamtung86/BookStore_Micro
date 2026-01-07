package com.bookstore.identity.service.Impl;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.identity.config.RolePermissionConfig;
import com.bookstore.identity.dto.LoginRequest;
import com.bookstore.identity.dto.LoginResponse;
import com.bookstore.identity.dto.RegisterRequest;
import com.bookstore.identity.dto.UserDTO;
import com.bookstore.identity.entity.User;
import com.bookstore.identity.entity.UserRole;
import com.bookstore.identity.repository.UserRepository;
import com.bookstore.identity.service.IAuthService;
import com.bookstore.identity.service.IJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.CUSTOMER) // Default role
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return mapToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("Account is disabled");
        }

        Set<String> permissions = RolePermissionConfig.getPermissionsForRole(user.getRole())
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId(),
                permissions
        );

        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        log.info("User logged in successfully: {}", user.getUsername());

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");

        try {
            String username = jwtService.extractUsername(refreshToken);
            String tokenType = jwtService.getTokenType(refreshToken);

            if (!"REFRESH".equals(tokenType)) {
                throw new BusinessException("Invalid token type. Refresh token required.");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("User not found"));

            if (!user.isEnabled()) {
                throw new BusinessException("Account is disabled");
            }

            if (!jwtService.validateToken(refreshToken, username)) {
                throw new BusinessException("Invalid or expired refresh token");
            }

            Set<String> permissions = RolePermissionConfig.getPermissionsForRole(user.getRole())
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            String newAccessToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    user.getRole().name(),
                    user.getId(),
                    permissions
            );

            String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

            log.info("Token refreshed successfully for user: {}", user.getUsername());

            return LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                    .build();

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new BusinessException("Invalid or expired refresh token");
        }
    }

    private UserDTO mapToDTO(User user) {
        Set<String> permissions = RolePermissionConfig.getPermissionsForRole(user.getRole())
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .permissions(permissions)
                .build();
    }
}
