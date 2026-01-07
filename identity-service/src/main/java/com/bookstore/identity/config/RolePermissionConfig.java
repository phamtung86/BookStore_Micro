package com.bookstore.identity.config;

import com.bookstore.identity.entity.Permission;
import com.bookstore.identity.entity.UserRole;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps roles to their permissions
 */
public class RolePermissionConfig {

    public static Set<Permission> getPermissionsForRole(UserRole role) {
        return switch (role) {
            case ADMIN -> new HashSet<>(Arrays.asList(

                    Permission.CREATE_USER,
                    Permission.VIEW_USER,
                    Permission.UPDATE_USER,
                    Permission.DELETE_USER,
                    Permission.VIEW_ALL_USERS,

                    Permission.CREATE_PRODUCT,
                    Permission.UPDATE_PRODUCT,
                    Permission.DELETE_PRODUCT,
                    Permission.VIEW_PRODUCT,

                    Permission.VIEW_ALL_ORDERS,
                    Permission.UPDATE_ORDER_STATUS,

                    Permission.VIEW_INVENTORY,
                    Permission.UPDATE_INVENTORY,
                    
                    // Admin permissions
                    Permission.MANAGE_ROLES,
                    Permission.VIEW_REPORTS,
                    Permission.MANAGE_SETTINGS
            ));
            
            case USER -> new HashSet<>(Arrays.asList(
                    Permission.VIEW_USER,
                    Permission.UPDATE_USER,

                    Permission.VIEW_PRODUCT,

                    Permission.CREATE_ORDER,
                    Permission.VIEW_OWN_ORDERS,
                    Permission.CANCEL_ORDER
            ));
            
            case CUSTOMER -> new HashSet<>(Arrays.asList(
                    Permission.VIEW_PRODUCT,
                    Permission.CREATE_ORDER,
                    Permission.VIEW_OWN_ORDERS,
                    Permission.CANCEL_ORDER
            ));
        };
    }
}
