package com.mavic.backend.controller;

import com.mavic.backend.dto.RejectOrderRequest;
import com.mavic.backend.exception.OrderException;
import com.mavic.backend.model.Order;
import com.mavic.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/kitchen")
public class KitchenController {
    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<?> getPendingOrders(
            @RequestParam(required = false) Long restaurantId
    ) {
        List<Order> orders = orderService.getPendingOrders(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable Long id) {
        Order order = orderService.acceptOrder(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/orders/{id}/ready")
    public ResponseEntity<?> markOrderReady(@PathVariable Long id) {
        Order order = orderService.markOrderReady(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/orders/{id}/reject")
    public ResponseEntity<?> rejectOrder(
            @PathVariable Long id,
            @Valid @RequestBody RejectOrderRequest request
    ) {
        Order order = orderService.rejectOrder(id, request.getReason());
        return ResponseEntity.ok(order);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<?> handleOrderException(OrderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
