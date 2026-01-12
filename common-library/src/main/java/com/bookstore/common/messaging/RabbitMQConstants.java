package com.bookstore.common.messaging;

public final class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    // ==================== EXCHANGES ====================
    public static final String FILE_EXCHANGE = "file.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";

    // ==================== FILE QUEUES ====================
    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    public static final String FILE_UPLOAD_RESULT_QUEUE = "file.upload.result.queue";

    // ==================== FILE ROUTING KEYS ====================
    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload";
    public static final String FILE_UPLOAD_RESULT_ROUTING_KEY = "file.upload.result";
    public static final String FILE_DELETE_ROUTING_KEY = "file.delete";
    public static final String ORDER_CREATE_FAIL = "order.create.fail";
    public static final String INVENTORY_OUT_OF_STOCK_KEY = "inventory.outofstock";
    public static final String ORDER_CREATE_SUCCESS_KEY = "order.create.success";
    // ==================== ORDER QUEUES ====================
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_FAIL_QUEUE = "order.create.fail.queue";
    public static final String ORDER_CREATE_SUCCESS_QUEUE = "order.create.success.queue";
    // ==================== NOTIFICATION QUEUES ====================

    // ==================== INVENTORY QUEUES ====================
    public static final String INVENTORY_STOCK_QUEUE = "inventory.stock.queue";

    public static final String PROCESS_PAYMENT_SUCCESS_QUEUE = "payment.success.queue";
    public static final String PROCESS_PAYMENT_FAIL_QUEUE = "payment.fail.queue";

    // ==================== SAGA EXCHANGE ====================


    // ==================== SAGA QUEUES ====================
    public static final String SAGA_ORDER_QUEUE = "saga.order.queue";
    public static final String SAGA_INVENTORY_OUT_OFF_STOCK_QUEUE = "saga.inventory.outofstock.queue";


}
