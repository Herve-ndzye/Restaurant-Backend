package com.mavic.backend.order.exception;

import com.mavic.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderException extends BusinessException {

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, HttpStatus status) {
        super(message, status);
    }
}
