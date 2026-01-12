package com.bookstore.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private int statusCode;
    private String message;
    private Object data;

    public static ServiceResponse RESPONSE_SUCCESS(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(ErrorCodes.SUCCESS.message());
        response.setStatus(ErrorCodes.SUCCESS.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_SUCCESS(String message, Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(message);
        response.setStatus(ErrorCodes.SUCCESS.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_ERROR(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ErrorCodes.BAD_REQUEST.message());
        response.setStatus(ErrorCodes.BAD_REQUEST.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_ERROR(String message, Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(message);
        response.setStatus(ErrorCodes.BAD_REQUEST.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_FORBIDDEN(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN.value());
        response.setMessage(ErrorCodes.FORBIDDEN.message());
        response.setStatus(ErrorCodes.FORBIDDEN.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_INTERNAL_SERVER_ERROR(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(ErrorCodes.INTERNAL_SERVER_ERROR.message());
        response.setStatus(ErrorCodes.INTERNAL_SERVER_ERROR.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_NOT_FOUND(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(ErrorCodes.NOT_FOUND.message());
        response.setStatus(ErrorCodes.NOT_FOUND.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_NOT_FOUND(String message, Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(message);
        response.setStatus(ErrorCodes.NOT_FOUND.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_UNAUTHORIZED(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        response.setMessage(ErrorCodes.UNAUTHORIZED.message());
        response.setStatus(ErrorCodes.UNAUTHORIZED.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_CONFLICT(Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.CONFLICT.value());
        response.setMessage(ErrorCodes.CONFLICT.message());
        response.setStatus(ErrorCodes.CONFLICT.status());
        response.setData(data);
        return response;
    }

    public static ServiceResponse RESPONSE_CONFLICT(String message, Object data) {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(HttpStatus.CONFLICT.value());
        response.setMessage(message);
        response.setStatus(ErrorCodes.CONFLICT.status());
        response.setData(data);
        return response;
    }
}
