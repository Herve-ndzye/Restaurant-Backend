package com.mavic.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class CustomerException extends RuntimeException {

    public CustomerException(String message) {
        throw new CustomerException(message);
    }
}
