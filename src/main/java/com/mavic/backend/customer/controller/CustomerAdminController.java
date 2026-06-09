package com.mavic.backend.customer.controller;

import com.mavic.backend.auth.dto.AdminInvitationResponse;
import com.mavic.backend.auth.dto.CreateAdminRequest;
import com.mavic.backend.auth.dto.RegisterDeliveryDriverRequest;
import com.mavic.backend.auth.dto.RegisterKitchenStaffRequest;
import com.mavic.backend.auth.service.AuthService;
import com.mavic.backend.customer.service.CustomerService;
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

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "5. Admin", description = "Customer management, menu management, staff registration, and admin invitations")
public class CustomerAdminController {
    private CustomerService customerService;
    private AuthService authService;

    @Operation(
            summary = "Register kitchen staff",
            description = "RESTAURANT_ADMIN ONLY — Create a kitchen staff account linked to a restaurant.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/register/kitchen-staff")
    public ResponseEntity<?> registerKitchenStaff(@Valid @RequestBody RegisterKitchenStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerKitchenStaff(request));
    }

    @Operation(
            summary = "Register delivery driver",
            description = "RESTAURANT_ADMIN ONLY — Create a delivery driver account.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/register/delivery-driver")
    public ResponseEntity<?> registerDeliveryDriver(@Valid @RequestBody RegisterDeliveryDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerDeliveryDriver(request));
    }

    @Operation(
            summary = "Get all customers (paginated)",
            description = """
                    Retrieve paginated list of all customers with their profiles.
                    
                    **RESTAURANT_ADMIN ONLY** - Regular customers cannot view other customers' information.
                    
                    This endpoint is useful for:
                    - Restaurant administrators to view customer database
                    - Analytics and reporting
                    - Customer support
                    - Marketing campaigns
                    
                    Returns customer profiles with delivery addresses and contact information.
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "name": "John Doe",
                                          "phone": "+1234567890",
                                          "address": "123 Main St, City, State",
                                          "createdAt": "2026-01-15T10:30:00"
                                        },
                                        {
                                          "id": 2,
                                          "name": "Jane Smith",
                                          "phone": "+0987654321",
                                          "address": "456 Oak Ave, Town, State",
                                          "createdAt": "2026-01-20T14:20:00"
                                        }
                                      ],
                                      "pageable": {
                                        "pageNumber": 0,
                                        "pageSize": 10
                                      },
                                      "totalElements": 45,
                                      "totalPages": 5
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Only restaurant administrators can view all customers",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-06-04T10:15:30.123+00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have permission to access this resource",
                                      "path": "/api/admin/customers"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No customers found",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-06-04T10:15:30.123",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "No customers found. The customer database is empty.",
                                      "path": "/api/admin/customers"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of customers per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        var customers = customerService.getAllCustomers(page, size);
        return ResponseEntity.ok().body(customers);
    }

    @Operation(
            summary = "Create admin invitation",
            description = """
                    **RESTAURANT_ADMIN ONLY** - Create a new admin account and generate temporary credentials.
                    
                    This endpoint allows existing administrators to invite new administrators:
                    1. Admin provides new admin's details (username, email, name)
                    2. System generates a secure temporary password
                    3. Response includes username and temporary password
                    4. Existing admin shares these credentials with new admin
                    5. New admin logs in and must change password on first login
                    
                    **Security features:**
                    - Temporary password auto-generated with strong requirements
                    - First login flag forces password change
                    - Audit log records who created the invitation
                    - Only RESTAURANT_ADMIN role can create invitations
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin invitation created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminInvitationResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": 10,
                                      "username": "new_admin",
                                      "email": "newadmin@restaurant.com",
                                      "temporaryPassword": "Xy3$aB9z@K",
                                      "loginUrl": "/api/auth/login",
                                      "message": "Admin account created successfully. Please share these credentials with the new admin and ask them to change their password on first login.",
                                      "requirePasswordChange": true
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-06-04T10:15:30.123",
                                      "status": 400,
                                      "error": "Validation Failed",
                                      "message": "Invalid input data",
                                      "details": {
                                        "email": "Email must be valid"
                                      },
                                      "path": "/api/admin/create-admin"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Only RESTAURANT_ADMIN can create invitations",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-06-04T10:15:30.123",
                                      "status": 403,
                                      "error": "Access Denied",
                                      "message": "Only restaurant administrators can create admin invitations",
                                      "path": "/api/admin/create-admin"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-06-04T10:15:30.123",
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Username 'new_admin' is already taken. Please choose a different username.",
                                      "path": "/api/admin/create-admin"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminInvitation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New admin details",
                    content = @Content(
                            schema = @Schema(implementation = CreateAdminRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "new_admin",
                                      "email": "newadmin@restaurant.com",
                                      "name": "Jane Doe",
                                      "restaurantId": 1
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CreateAdminRequest request) {
        AdminInvitationResponse response = authService.createAdminInvitation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
