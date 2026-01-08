package com.bookstore.common.messaging;

public final class
RabbitMQConstants {

    private RabbitMQConstants() {
    }

    // ==================== EXCHANGES ====================
    public static final String FILE_EXCHANGE = "file.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";

    // ==================== FILE QUEUES ====================
    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    public static final String FILE_UPLOAD_RESULT_QUEUE = "file.upload.result.queue";

    // ==================== FILE ROUTING KEYS ====================
    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload";
    public static final String FILE_UPLOAD_RESULT_ROUTING_KEY = "file.upload.result";

    // ==================== ORDER QUEUES ====================
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";

    // ==================== NOTIFICATION QUEUES ====================
    public static final String EMAIL_QUEUE = "notification.email.queue";
    public static final String SMS_QUEUE = "notification.sms.queue";
    public static final String PUSH_QUEUE = "notification.push.queue";

    // ==================== INVENTORY QUEUES ====================
    public static final String INVENTORY_RESERVE_QUEUE = "inventory.reserve.queue";
    public static final String INVENTORY_RELEASE_QUEUE = "inventory.release.queue";
    public static final String INVENTORY_DEDUCT_QUEUE = "inventory.deduct.queue";
}
