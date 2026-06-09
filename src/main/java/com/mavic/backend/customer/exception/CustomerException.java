package com.mavic.backend.customer.exception;

import com.mavic.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CustomerException extends BusinessException {

    public CustomerException(String message) {
        super(message);
    }

    public CustomerException(String message, HttpStatus status) {
        super(message, status);
    }
}
