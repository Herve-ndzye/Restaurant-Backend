package com.mavic.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, HttpStatus status) {
        this(message, status, errorLabelFor(status));
    }

    public BusinessException(String message, HttpStatus status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

    private static String errorLabelFor(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Not Found";
            case FORBIDDEN -> "Access Denied";
            case CONFLICT -> "Conflict";
            case BAD_REQUEST -> "Bad Request";
            default -> "Business Error";
        };
    }
}
