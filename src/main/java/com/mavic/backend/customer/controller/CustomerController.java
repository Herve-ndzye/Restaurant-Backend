package com.mavic.backend.customer.controller;

import com.mavic.backend.customer.exception.CustomerException;
import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.service.CustomerService;
import com.mavic.backend.customer.dto.NewCustomerDto;
import com.mavic.backend.customer.dto.ProfileUpdateDto;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/customer")
@Tag(name = "2. Customer", description = "Customer profile management and order operations")
public class CustomerController {
    private CustomerService customerService;

    @Operation(
            summary = "Get customer profile",
            description = "Retrieve customer profile details. Requires CUSTOMER role. Customer can only view their own profile.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer profile retrieved",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this profile"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable("id") Long id
    ){
        var customer = customerService.getCustomer(id);
        return ResponseEntity
                .status(200)
                .body(customer);
    }

    @Operation(
            summary = "Update customer profile",
            description = "Update customer profile information. Requires CUSTOMER role. Customer can only update their own profile.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this profile"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated profile details",
                    content = @Content(
                            schema = @Schema(implementation = ProfileUpdateDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "phone": "+1234567890",
                                      "address": "456 New St, City, State 12345"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody ProfileUpdateDto profile
    ){
        var customer = customerService.updateCustomer(id,profile);
        return ResponseEntity
                .status(200)
                .body(customer);
    }

    @Operation(
            summary = "Deactivate customer account",
            description = """
                    Deactivate customer account (soft delete). This prevents the customer from logging in while preserving:
                    - Customer profile data
                    - Order history
                    - All related records
                    
                    The account can be reactivated by an administrator if needed. Requires CUSTOMER role. Customer can only deactivate their own account.
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account deactivated successfully",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Account deactivated successfully. All your data has been preserved."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Account is already deactivated",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "Account is already deactivated"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to deactivate this account"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCustomer(
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable("id") Long id
    ){
        customerService.deactivateCustomer(id);
        return ResponseEntity
                .ok()
                .body(
                        Map.of(
                                "message", "Account deactivated successfully. All your data has been preserved.",
                                "note", "Contact support if you wish to reactivate your account"
                        )
                );
    }

    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<?> handleCustomerException(CustomerException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of("Error", ex.getMessage())
                );
    }

}
