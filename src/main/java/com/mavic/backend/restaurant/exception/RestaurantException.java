package com.mavic.backend.restaurant.exception;

import com.mavic.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RestaurantException extends BusinessException {

    public RestaurantException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public RestaurantException(String message, HttpStatus status) {
        super(message, status);
    }
}
