package com.mavic.backend.auth.controller;

import com.mavic.backend.auth.dto.*;
import com.mavic.backend.auth.exception.AccountLockedException;
import com.mavic.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication", description = "Registration and login for all user roles")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Login to get JWT token",
            description = "Authenticate user and receive JWT token for accessing protected endpoints. Rate limited to 5 requests per minute."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "id": 1,
                                      "username": "john_doe",
                                      "email": "john@example.com",
                                      "role": "CUSTOMER"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "Invalid username or password"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account locked due to multiple failed attempts",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "Account is locked due to multiple failed login attempts. Please try again later."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "Too many requests. Please try again later."
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (LockedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (AccountLockedException e) {
            if (e.isLocked()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", e.getMessage(),
                                "locked", true
                        ));
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", e.getMessage(),
                                "attemptsRemaining", e.getAttemptsRemaining()
                        ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @Operation(
            summary = "Register a new customer",
            description = "Register a customer account with delivery information in a single step. All customer details including delivery address are collected at registration."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer registration details",
                    content = @Content(
                            schema = @Schema(implementation = RegisterCustomerRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "john_doe",
                                      "email": "john@example.com",
                                      "password": "SecurePass123!",
                                      "name": "John Doe",
                                      "phone": "+1234567890",
                                      "address": "123 Main St, City, State 12345"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody RegisterCustomerRequest request) {
        try {
            AuthResponse response = authService.registerCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Register a new kitchen staff member",
            description = "Register a kitchen staff account linked to a specific restaurant. Restaurant ID must be valid."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Kitchen staff registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input, user already exists, or restaurant not found")
    })
    @PostMapping("/register/kitchen-staff")
    public ResponseEntity<?> registerKitchenStaff(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Kitchen staff registration details",
                    content = @Content(
                            schema = @Schema(implementation = RegisterKitchenStaffRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "kitchen_staff1",
                                      "email": "staff@restaurant.com",
                                      "password": "StaffPass123!",
                                      "name": "Jane Smith",
                                      "restaurantId": 1
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody RegisterKitchenStaffRequest request) {
        try {
            AuthResponse response = authService.registerKitchenStaff(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Register a new delivery driver",
            description = "Register a delivery driver account with vehicle information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Delivery driver registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping("/register/delivery-driver")
    public ResponseEntity<?> registerDeliveryDriver(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Delivery driver registration details",
                    content = @Content(
                            schema = @Schema(implementation = RegisterDeliveryDriverRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "driver_mike",
                                      "email": "driver@delivery.com",
                                      "password": "DriverPass123!",
                                      "name": "Mike Johnson",
                                      "phone": "+1234567890",
                                      "vehicleType": "Motorcycle",
                                      "vehicleNumber": "ABC-1234"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody RegisterDeliveryDriverRequest request) {
        try {
            AuthResponse response = authService.registerDeliveryDriver(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Register a new admin (requires invite code)",
            description = "Register a restaurant admin account. Requires a valid invite code for security. Contact system administrator for invite code."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input, invalid invite code, or user already exists")
    })
    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin registration details with invite code",
                    content = @Content(
                            schema = @Schema(implementation = RegisterAdminRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "admin_user",
                                      "email": "admin@restaurant.com",
                                      "password": "AdminPass123!",
                                      "name": "Admin User",
                                      "inviteCode": "ADMIN-SECRET-2026"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody RegisterAdminRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Change password",
            description = """
                    Change password for the currently authenticated user.
                    
                    Required for first-time login after admin invitation.
                    Can also be used by any authenticated user to change their password.
                    
                    **Requirements:**
                    - Must provide correct current password
                    - New password must meet security requirements
                    - New password must be different from current password
                    - New password must match confirmation
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Password changed successfully"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or password requirements not met",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Incorrect current password",
                                            value = """
                                            {
                                              "error": "Current password is incorrect"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Password mismatch",
                                            value = """
                                            {
                                              "error": "New password and confirmation do not match"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Same password",
                                            value = """
                                            {
                                              "error": "New password must be different from current password"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "Unauthorized"
                                    }
                                    """)
                    )
            )
    })
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password change details",
                    content = @Content(
                            schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "currentPassword": "TempPass123!",
                                      "newPassword": "MyNewSecurePass123!",
                                      "confirmPassword": "MyNewSecurePass123!"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
