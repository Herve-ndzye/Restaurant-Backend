package com.mavic.backend.auth.exception;

import com.mavic.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, HttpStatus status) {
        super(message, status);
    }
}
