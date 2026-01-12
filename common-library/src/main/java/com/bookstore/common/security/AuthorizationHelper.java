package com.bookstore.common.security;

import com.bookstore.common.exception.BusinessException;

public class AuthorizationHelper {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SELLER = "SELLER";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equals(role);
    }

    public static boolean isSeller(String role) {
        return ROLE_SELLER.equals(role) || ROLE_ADMIN.equals(role);
    }

    public static boolean isCustomer(String role) {
        return ROLE_CUSTOMER.equals(role);
    }

    public static boolean hasAnyRole(String role, String... allowedRoles) {
        if (role == null)
            return false;
        for (String allowedRole : allowedRoles) {
            if (role.equals(allowedRole))
                return true;
        }
        return false;
    }

    public static void requireAdmin(String role) {
        if (!isAdmin(role)) {
            throw new BusinessException("Access denied. Admin role required.");
        }
    }

    public static void requireSeller(String role) {
        if (!isSeller(role)) {
            throw new BusinessException("Access denied. Seller or Admin role required.");
        }
    }

    public static void requireCustomer(String role) {
        if (!isCustomer(role)) {
            throw new BusinessException("Access denied. Customer role required.");
        }
    }

    public static void requireAnyRole(String role, String... allowedRoles) {
        if (!hasAnyRole(role, allowedRoles)) {
            throw new BusinessException("Access denied. Required roles: " + String.join(", ", allowedRoles));
        }
    }

    public static void requireOwnerOrAdmin(Long resourceOwnerId, Long userId, String role) {
        if (!resourceOwnerId.equals(userId) && !isAdmin(role)) {
            throw new BusinessException("Access denied. You don't have permission to access this resource.");
        }
    }
}