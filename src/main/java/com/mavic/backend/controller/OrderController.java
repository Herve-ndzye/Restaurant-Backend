package com.mavic.backend.controller;

import com.mavic.backend.dto.OrderRequest;
import com.mavic.backend.exception.OrderException;
import com.mavic.backend.model.Order;
import com.mavic.backend.model.enums.OrderStatus;
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
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerOrders(@PathVariable Long customerId) {
        List<Order> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
    }

    @PutMapping("/{id}/picked-up")
    public ResponseEntity<?> markOrderPickedUp(@PathVariable Long id) {
        Order order = orderService.markOrderPickedUp(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/delivered")
    public ResponseEntity<?> markOrderDelivered(@PathVariable Long id) {
        Order order = orderService.markOrderDelivered(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long id) {
        OrderStatus status = orderService.getOrderStatus(id);
        return ResponseEntity.ok(Map.of("status", status));
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<?> handleOrderException(OrderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
