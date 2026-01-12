package com.bookstore.common.dto.response;

import org.springframework.http.HttpStatus;

public enum ErrorCodes {
	SUCCESS("SUCCESS", "Success", HttpStatus.OK.value()),
	BAD_REQUEST("BAD_REQUEST", "Bad request", HttpStatus.BAD_REQUEST.value()),
	UNAUTHORIZED("UNAUTHORIZED", "Unauthorized", HttpStatus.UNAUTHORIZED.value()),
	FORBIDDEN("FORBIDDEN", "Forbidden", HttpStatus.FORBIDDEN.value()),
	NOT_FOUND("NOT_FOUND", "Not Found", HttpStatus.NOT_FOUND.value()),
	INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value()),
	CONFLICT("CONFLICT", "Resource already exists or conflict occurred", HttpStatus.CONFLICT.value()),

	// User related errors
	USER_NOT_EXISTS("USER_NOT_EXISTS", "User not exists", HttpStatus.BAD_REQUEST.value()),
	USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists", HttpStatus.BAD_REQUEST.value()),
	USER_LOCKED("USER_LOCKED", "User locked", HttpStatus.BAD_REQUEST.value()),
	INVALID_USER_PASSWORD("INVALID_USER_PASSWORD", "Username or password is incorrect", HttpStatus.BAD_REQUEST.value()),

	// Validation errors
	INVALID_EMAIL("INVALID_EMAIL", "Invalid email", HttpStatus.BAD_REQUEST.value()),
	EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.BAD_REQUEST.value()),
	INVALID_PASSWORD("INVALID_PASSWORD", "Invalid password", HttpStatus.BAD_REQUEST.value()),
	WEAK_PASSWORD("WEAK_PASSWORD", "Weak password", HttpStatus.BAD_REQUEST.value()),

	// Token errors
	REFRESH_TOKEN_ERROR("REFRESH_TOKEN_ERROR", "Refresh token error", HttpStatus.BAD_REQUEST.value()),
	EXPIRED_TOKEN("EXPIRED_TOKEN", "Token has expired", HttpStatus.UNAUTHORIZED.value()),
	INVALID_TOKEN("INVALID_TOKEN", "Invalid token", HttpStatus.UNAUTHORIZED.value()),

	// Product related errors
	PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND.value()),
	CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND.value()),
	PUBLISHER_NOT_FOUND("PUBLISHER_NOT_FOUND", "Publisher not found", HttpStatus.NOT_FOUND.value()),

	// Order related errors
	ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND.value()),
	INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", "Invalid order status", HttpStatus.BAD_REQUEST.value()),

	// Payment related errors
	PAYMENT_FAILED("PAYMENT_FAILED", "Payment failed", HttpStatus.BAD_REQUEST.value()),
	INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient balance", HttpStatus.BAD_REQUEST.value()),

	// Inventory related errors
	OUT_OF_STOCK("OUT_OF_STOCK", "Product is out of stock", HttpStatus.BAD_REQUEST.value()),
	INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "Insufficient stock", HttpStatus.BAD_REQUEST.value());

	private final int statusCode;
	private final String message;
	private final String status;

	ErrorCodes(String status, String message, int statusCode) {
		this.statusCode = statusCode;
		this.message = message;
		this.status = status;
	}

	public int statusCode() {
		return statusCode;
	}

	public String message() {
		return message;
	}

	public String status() {
		return status;
	}
}
