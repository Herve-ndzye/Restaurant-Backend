package com.mavic.backend.delivery.controller;

import com.mavic.backend.order.exception.OrderException;
import com.mavic.backend.order.model.Order;
import com.mavic.backend.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/delivery")
@Tag(name = "Delivery Operations", description = "Delivery driver operations for order fulfillment")
public class DeliveryController {
    private final OrderService orderService;

    @Operation(
            summary = "Mark order as picked up",
            description = "Mark order as picked up by delivery driver. Requires DELIVERY_DRIVER role. Only READY orders can be picked up.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order marked as picked up",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "400", description = "Order cannot be picked up (not in READY status)"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/orders/{id}/picked-up")
    public ResponseEntity<?> markOrderPickedUp(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        Order order = orderService.markOrderPickedUp(id);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Mark order as delivered",
            description = "Mark order as delivered to customer. Requires DELIVERY_DRIVER role. Only PICKED_UP orders can be marked as delivered.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order marked as delivered",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "400", description = "Order cannot be delivered (not in PICKED_UP status)"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/orders/{id}/delivered")
    public ResponseEntity<?> markOrderDelivered(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        Order order = orderService.markOrderDelivered(id);
        return ResponseEntity.ok(order);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<?> handleOrderException(OrderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
