package com.mavic.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateDto {
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{7,20}$", message = "Invalid phone number format")
    private String phone = null;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address = null;
}
