package com.mavic.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectOrderRequest {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
