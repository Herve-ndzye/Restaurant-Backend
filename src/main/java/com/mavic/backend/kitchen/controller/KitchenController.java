package com.mavic.backend.kitchen.controller;

import com.mavic.backend.order.dto.RejectOrderRequest;
import com.mavic.backend.order.exception.OrderException;
import com.mavic.backend.order.model.Order;
import com.mavic.backend.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Kitchen Operations", description = "Kitchen staff operations for managing orders")
public class KitchenController {
    private final OrderService orderService;

    @Operation(
            summary = "Get pending orders",
            description = "Retrieve all pending orders. Kitchen staff can only view orders for their restaurant. Requires KITCHEN_STAFF role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to view orders for this restaurant")
    })
    @GetMapping("/orders")
    public ResponseEntity<?> getPendingOrders(
            @Parameter(description = "Filter by restaurant ID (optional)", example = "1")
            @RequestParam(required = false) Long restaurantId
    ) {
        List<Order> orders = orderService.getPendingOrders(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Accept order",
            description = "Accept a pending order and start preparing. Requires KITCHEN_STAFF role. Staff can only accept orders for their restaurant. Order status changes: PENDING → ACCEPTED → PREPARING",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order accepted successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "400", description = "Order cannot be accepted (not in PENDING status)"),
            @ApiResponse(responseCode = "403", description = "Not authorized to accept this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<?> acceptOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        Order order = orderService.acceptOrder(id);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Mark order as ready",
            description = "Mark order as ready for pickup. Requires KITCHEN_STAFF role. Staff can only mark orders ready for their restaurant. Only PREPARING orders can be marked as READY.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order marked as ready",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "400", description = "Order cannot be marked ready (not in PREPARING status)"),
            @ApiResponse(responseCode = "403", description = "Not authorized to mark this order ready"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/orders/{id}/ready")
    public ResponseEntity<?> markOrderReady(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        Order order = orderService.markOrderReady(id);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Reject order",
            description = "Reject a pending order with reason. Requires KITCHEN_STAFF role. Staff can only reject orders for their restaurant. Only PENDING orders can be rejected.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order rejected successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "400", description = "Order cannot be rejected (not in PENDING status)"),
            @ApiResponse(responseCode = "403", description = "Not authorized to reject this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/orders/{id}/reject")
    public ResponseEntity<?> rejectOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rejection reason",
                    content = @Content(
                            schema = @Schema(implementation = RejectOrderRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "reason": "Out of ingredients for requested items"
                                    }
                                    """)
                    )
            )
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
