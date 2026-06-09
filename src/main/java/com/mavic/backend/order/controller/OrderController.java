package com.mavic.backend.order.controller;

import com.mavic.backend.common.enums.OrderStatus;
import com.mavic.backend.order.dto.OrderRequest;
import com.mavic.backend.order.dto.OrderResponse;
import com.mavic.backend.order.mapper.OrderMapper;
import com.mavic.backend.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "2. Customer", description = "Customer orders and order tracking")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Operation(
            summary = "Place a new order",
            description = "Create a new order. Requires CUSTOMER role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order placed successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid order data"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        var order = orderService.placeOrder(request);
        order = orderService.getOrderById(order.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @Operation(
            summary = "Get order by ID",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.getOrderById(id)));
    }

    @Operation(
            summary = "Get customer orders (paginated)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(
            @PathVariable Long customerId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "10") int size) {
        Page<OrderResponse> orders = orderService.getCustomerOrders(customerId, page, size)
                .map(orderMapper::toResponse);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Cancel order",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
    }

    @Operation(
            summary = "Get order status",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, OrderStatus>> getOrderStatus(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", orderService.getOrderStatus(id)));
    }
}
