package com.mavic.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "User registration request")
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscore, and hyphen")
    @Schema(description = "Unique username for login", example = "john_doe", required = true)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    @Schema(
            description = "Strong password (min 8 chars, must include: uppercase, lowercase, digit, special character)",
            example = "SecurePass123!",
            required = true
    )
    private String password;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Schema(description = "Valid email address", example = "john.doe@example.com", required = true)
    private String email;
    
    @NotNull(message = "Role is required")
    @Pattern(regexp = "CUSTOMER|RESTAURANT_ADMIN|KITCHEN_STAFF|DELIVERY_DRIVER", message = "Invalid role")
    @Schema(
            description = "User role",
            example = "CUSTOMER",
            required = true,
            allowableValues = {"CUSTOMER", "RESTAURANT_ADMIN", "KITCHEN_STAFF", "DELIVERY_DRIVER"}
    )
    private String role;
    
    @Schema(
            description = "Optional: Link to existing Customer profile. Only use if the Customer profile already exists. For new customers, omit this field and create Customer profile separately via /api/customer/register.",
            example = "null",
            nullable = true
    )
    private Long customerId;
    
    @Schema(
            description = "Optional: Link to Restaurant for staff members. Required for RESTAURANT_ADMIN and KITCHEN_STAFF roles. Omit for CUSTOMER and DELIVERY_DRIVER roles.",
            example = "1",
            nullable = true
    )
    private Long restaurantId;
}
