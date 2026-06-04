package com.mavic.backend.controller;

import com.mavic.backend.dto.OrderRequest;
import com.mavic.backend.exception.OrderException;
import com.mavic.backend.model.Order;
import com.mavic.backend.model.enums.OrderStatus;
import com.mavic.backend.service.OrderService;
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
@RequestMapping("/api/orders")
@Tag(name = "Customer Orders", description = "Customer operations for placing and tracking orders")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "Place a new order",
            description = "Create a new order for a customer. Requires CUSTOMER role. Customer can only place orders for their own account.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order placed successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid order data or restaurant closed"),
            @ApiResponse(responseCode = "403", description = "Not authorized to place order for this customer"),
            @ApiResponse(responseCode = "404", description = "Customer, restaurant, or menu item not found")
    })
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order details",
                    content = @Content(
                            schema = @Schema(implementation = OrderRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "customerId": 1,
                                      "restaurantId": 1,
                                      "items": [
                                        {
                                          "menuItemId": 1,
                                          "quantity": 2
                                        },
                                        {
                                          "menuItemId": 3,
                                          "quantity": 1
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieve order details. Customers can only view their own orders. Kitchen staff can view orders for their restaurant.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Get customer orders",
            description = "Retrieve all orders for a specific customer. Requires CUSTOMER role. Customer can only view their own orders.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to view these orders"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerOrders(
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable Long customerId) {
        List<Order> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancel a pending order. Requires CUSTOMER role. Only PENDING orders can be cancelled. Customer can only cancel their own orders.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Order cancelled successfully"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled (not in PENDING status)"),
            @ApiResponse(responseCode = "403", description = "Not authorized to cancel this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
    }

    @Operation(
            summary = "Get order status",
            description = "Get current status of an order. Requires authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status retrieved",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "PREPARING"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getOrderStatus(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
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
