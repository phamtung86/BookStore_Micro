package com.bookstore.common.exception;

import com.bookstore.common.dto.response.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ServiceResponse> handleBusinessException(BusinessException ex) {
        log.error("Business Exception: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ServiceResponse.RESPONSE_ERROR(ex.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ServiceResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource Not Found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ServiceResponse.RESPONSE_ERROR(ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ServiceResponse.RESPONSE_ERROR("Internal server error: " + ex.getMessage(), null));
    }
}
