package com.bookstore.identity.service.Impl;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.identity.config.RolePermissionConfig;
import com.bookstore.identity.dto.UserDTO;
import com.bookstore.identity.entity.User;
import com.bookstore.identity.repository.UserRepository;
import com.bookstore.identity.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ServiceResponse.RESPONSE_SUCCESS("Users retrieved successfully", users);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        return ServiceResponse.RESPONSE_SUCCESS("User retrieved successfully", mapToDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        return ServiceResponse.RESPONSE_SUCCESS("Profile retrieved successfully", mapToDTO(user));
    }

    @Override
    @Transactional
    public ServiceResponse deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("User not found");
        }
        userRepository.deleteById(id);
        return ServiceResponse.RESPONSE_SUCCESS("User deleted successfully", null);
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
