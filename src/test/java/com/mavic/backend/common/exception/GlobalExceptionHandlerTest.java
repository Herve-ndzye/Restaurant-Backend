package com.mavic.backend.common.exception;

import com.mavic.backend.auth.exception.AuthException;
import com.mavic.backend.order.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleBusinessException_ReturnsCorrectStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new AuthException("Username already taken", HttpStatus.CONFLICT),
                webRequest
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Username already taken", response.getBody().getMessage());
    }

    @Test
    void handleOrderException_AsBusinessException() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new OrderException("Order not found", HttpStatus.NOT_FOUND),
                webRequest
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Order not found", response.getBody().getMessage());
    }
}
