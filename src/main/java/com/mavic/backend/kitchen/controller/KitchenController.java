package com.mavic.backend.kitchen.controller;

import com.mavic.backend.order.dto.OrderResponse;
import com.mavic.backend.order.dto.RejectOrderRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/kitchen")
@Tag(name = "3. Kitchen Staff", description = "Kitchen order management and preparation workflow")
public class KitchenController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Operation(
            summary = "Get pending orders (paginated)",
            description = "Retrieve pending orders for the authenticated kitchen staff member's restaurant.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending orders retrieved",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponse>> getPendingOrders(
            @Parameter(description = "Optional restaurant ID override") @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderResponse> orders = orderService.getPendingOrders(restaurantId, page, size)
                .map(orderMapper::toResponse);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Accept order", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.acceptOrder(id)));
    }

    @Operation(summary = "Mark order as ready", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/orders/{id}/ready")
    public ResponseEntity<OrderResponse> markOrderReady(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.markOrderReady(id)));
    }

    @Operation(summary = "Reject order", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/orders/{id}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long id, @Valid @RequestBody RejectOrderRequest request) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.rejectOrder(id, request.getReason())));
    }
}
