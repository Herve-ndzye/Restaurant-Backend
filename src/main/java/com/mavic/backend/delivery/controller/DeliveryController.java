package com.mavic.backend.delivery.controller;

import com.mavic.backend.order.dto.OrderResponse;
import com.mavic.backend.order.mapper.OrderMapper;
import com.mavic.backend.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/delivery")
@Tag(name = "4. Delivery Driver", description = "Order pickup and delivery tracking")
public class DeliveryController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Operation(
            summary = "Mark order as picked up",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order picked up",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    })
    @PutMapping("/orders/{id}/picked-up")
    public ResponseEntity<OrderResponse> markOrderPickedUp(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.markOrderPickedUp(id)));
    }

    @Operation(
            summary = "Mark order as delivered",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/orders/{id}/delivered")
    public ResponseEntity<OrderResponse> markOrderDelivered(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.markOrderDelivered(id)));
    }
}
