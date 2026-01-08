package com.bookstore.identity.entity;

/**
 * Specific permissions/authorities for fine-grained access control
 */
public enum Permission {
    // User Management
    CREATE_USER,
    VIEW_USER,
    UPDATE_USER,
    DELETE_USER,
    VIEW_ALL_USERS,

    // Product Management
    CREATE_PRODUCT,
    UPDATE_PRODUCT,
    DELETE_PRODUCT,
    VIEW_PRODUCT,

    // Order Management
    CREATE_ORDER,
    VIEW_OWN_ORDERS,
    VIEW_ALL_ORDERS,
    UPDATE_ORDER_STATUS,
    CANCEL_ORDER,

    // Inventory Management
    VIEW_INVENTORY,
    UPDATE_INVENTORY,

    // Payment Management
    VIEW_PAYMENT,
    PROCESS_REFUND,

    // Promotion Management
    CREATE_PROMOTION,
    UPDATE_PROMOTION,
    DELETE_PROMOTION,
    VIEW_PROMOTION,

    // Notification Management
    CREATE_NOTIFICATION,
    VIEW_NOTIFICATION,

    // System Administration
    MANAGE_ROLES,
    VIEW_REPORTS,
    MANAGE_SETTINGS
}
