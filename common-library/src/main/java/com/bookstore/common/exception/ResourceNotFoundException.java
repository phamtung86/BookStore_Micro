package com.bookstore.common.exception;

/**
 * Exception thrown when resource is not found
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
