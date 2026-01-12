package com.bookstore.identity.service.Impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.identity.config.RolePermissionConfig;
import com.bookstore.identity.dto.LoginRequest;
import com.bookstore.identity.dto.LoginResponse;
import com.bookstore.identity.dto.RegisterRequest;
import com.bookstore.identity.dto.UserDTO;
import com.bookstore.identity.entity.User;
import com.bookstore.identity.entity.UserLoginHistory;
import com.bookstore.identity.entity.UserRole;
import com.bookstore.identity.repository.UserRepository;
import com.bookstore.identity.service.IAuthService;
import com.bookstore.identity.service.IJwtService;
import com.bookstore.identity.service.IUserLoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final IUserLoginHistoryService userLoginHistoryService;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;

    @Override
    @Transactional
    public ServiceResponse register(RegisterRequest request) {
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
        return ServiceResponse.RESPONSE_SUCCESS("User registered successfully", mapToDTO(savedUser));
    }

    @Override
    @Transactional
    public ServiceResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {}", request.getUsername());

        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            log.warn("Login failed - user not found: {}", request.getUsername());
            throw new BusinessException("Invalid username or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userLoginHistoryService.saveLoginHistory(user, ipAddress, userAgent,
                    UserLoginHistory.LoginStatus.FAILED, "Invalid password");
            log.warn("Login failed - invalid password for user: {}", request.getUsername());
            throw new BusinessException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            userLoginHistoryService.saveLoginHistory(user, ipAddress, userAgent,
                    UserLoginHistory.LoginStatus.BLOCKED, "Account disabled");
            throw new BusinessException("Account is disabled");
        }

        userLoginHistoryService.saveLoginHistory(user, ipAddress, userAgent,
                UserLoginHistory.LoginStatus.SUCCESS, null);

        Set<String> permissions = RolePermissionConfig.getPermissionsForRole(user.getRole())
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId(),
                permissions);

        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        LoginResponse loginResponse = LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .build();

        return ServiceResponse.RESPONSE_SUCCESS("Login successful", loginResponse);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse refreshToken(String refreshToken) {

        try {
            String username = jwtService.extractUsername(refreshToken);
            String tokenType = jwtService.getTokenType(refreshToken);

            if (!"REFRESH".equals(tokenType)) {
                throw new BusinessException("Invalid token type. Refresh token required.");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("User not found"));

            if (!Boolean.TRUE.equals(user.getEnabled())) {
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
                    permissions);

            String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                    .build();

            return ServiceResponse.RESPONSE_SUCCESS("Token refreshed successfully", loginResponse);

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
                .enabled(Boolean.TRUE.equals(user.getEnabled()))
                .createdAt(user.getCreatedAt())
                .permissions(permissions)
                .build();
    }
}
