package com.mavic.backend.auth.controller;

import com.mavic.backend.auth.dto.AuthResponse;
import com.mavic.backend.auth.dto.LoginRequest;
import com.mavic.backend.auth.dto.RegisterRequest;
import com.mavic.backend.auth.exception.AccountLockedException;
import com.mavic.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = """
                    Create a new user account with specified role.
                    
                    **Available roles:**
                    - CUSTOMER: End user who places orders
                    - RESTAURANT_ADMIN: Manages restaurant menu and settings
                    - KITCHEN_STAFF: Processes orders in the kitchen
                    - DELIVERY_DRIVER: Delivers orders to customers
                    
                    **Optional linking fields (customerId, restaurantId):**
                    - Only provide these if linking an existing Customer/Restaurant profile to a new User account
                    - For new customers: Omit customerId - you'll create the Customer profile separately via /api/customer/register
                    - For restaurant staff: Provide restaurantId to link staff to their restaurant
                    - Most registrations should leave these fields null/omitted
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful registration",
                                    value = """
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
                    responseCode = "400",
                    description = "Invalid input or user already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Username exists",
                                            value = """
                                            {
                                              "error": "Username already exists"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Email exists",
                                            value = """
                                            {
                                              "error": "Email already exists"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid password",
                                            value = """
                                            {
                                              "error": "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

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
}
