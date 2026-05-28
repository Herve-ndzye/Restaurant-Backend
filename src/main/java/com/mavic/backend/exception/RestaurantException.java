package com.mavic.backend.exception;

public class RestaurantException extends RuntimeException {
    public RestaurantException(String message) {
        throw new RestaurantException(message);
    }
}
