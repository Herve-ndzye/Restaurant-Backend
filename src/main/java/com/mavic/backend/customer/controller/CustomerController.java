package com.mavic.backend.customer.controller;

import com.mavic.backend.customer.dto.CustomerProfileResponse;
import com.mavic.backend.customer.dto.ProfileUpdateDto;
import com.mavic.backend.customer.mapper.CustomerProfileMapper;
import com.mavic.backend.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/customer")
@Tag(name = "2. Customer", description = "Customer profile management")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerProfileMapper customerProfileMapper;

    @Operation(summary = "Get customer profile", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(schema = @Schema(implementation = CustomerProfileResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerProfileResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerProfileMapper.toResponse(customerService.getCustomer(id)));
    }

    @Operation(summary = "Update customer profile", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/{id}")
    public ResponseEntity<CustomerProfileResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateDto profile) {
        return ResponseEntity.ok(customerProfileMapper.toResponse(customerService.updateCustomer(id, profile)));
    }

    @Operation(summary = "Deactivate customer account", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateCustomer(@PathVariable Long id) {
        customerService.deactivateCustomer(id);
        return ResponseEntity.ok(Map.of(
                "message", "Account deactivated successfully. All your data has been preserved.",
                "note", "Contact support if you wish to reactivate your account"
        ));
    }
}
