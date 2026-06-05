package com.mavic.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Delivery driver registration request")
public class RegisterDeliveryDriverRequest {
    
    // Authentication fields
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(description = "Unique username for login", example = "driver_mike")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User email address", example = "driver@delivery.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "Strong password", example = "DriverPass123!")
    private String password;

    // Delivery driver specific fields
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Driver full name", example = "Mike Johnson")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{7,20}$", message = "Invalid phone number format")
    @Schema(description = "Driver contact phone", example = "+1234567890")
    private String phone;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type cannot exceed 50 characters")
    @Schema(description = "Type of vehicle used for delivery", example = "Motorcycle")
    private String vehicleType;

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 20, message = "Vehicle number cannot exceed 20 characters")
    @Schema(description = "Vehicle registration/license number", example = "ABC-1234")
    private String vehicleNumber;
}
