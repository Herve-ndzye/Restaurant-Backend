package com.mavic.backend.controller;

import com.mavic.backend.exception.CustomerException;
import com.mavic.backend.model.Customer;
import com.mavic.backend.service.CustomerService;
import com.mavic.backend.dto.NewCustomerDto;
import com.mavic.backend.dto.ProfileUpdateDto;
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
@Tag(name = "Customer Profile", description = "Customer profile management operations")
public class CustomerController {
    private CustomerService customerService;

    @Operation(
            summary = "Register new customer",
            description = "Create a new customer profile. This is separate from user authentication registration.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer registered successfully",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or customer already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer details",
                    content = @Content(
                            schema = @Schema(implementation = NewCustomerDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "John Doe",
                                      "phone": "+1234567890",
                                      "address": "123 Main St, City, State 12345"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody NewCustomerDto customer,
            UriComponentsBuilder uriBuilder
    ){
        var newCustomer = customerService.register(customer);
        var uri =uriBuilder.path("/customers/{id}").buildAndExpand(newCustomer.getId()).toUri();
        return ResponseEntity.created(uri).body(newCustomer);
    }

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
            summary = "Delete customer profile",
            description = "Delete customer profile. Requires CUSTOMER role. Customer can only delete their own profile.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer deleted successfully",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "Message": "Customer deleted Successfully."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this profile"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable("id") Long id
    ){
        customerService.deleteCustomer(id);
        return ResponseEntity
                .status(200)
                .body(
                        Map.of("Message", "Customer deleted Successfully.")
                );
    }

    @Operation(
            summary = "Get all customers (paginated)",
            description = "Retrieve paginated list of all customers. Admin operation.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully"
            ),
            @ApiResponse(responseCode = "404", description = "No customers found")
    })
    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam("page") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam("size") int size
    ){
        var customers = customerService.getAllCustomers(page,size);
        return ResponseEntity
                .ok()
                .body(customers);
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
