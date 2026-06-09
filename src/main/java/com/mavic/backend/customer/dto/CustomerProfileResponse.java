package com.mavic.backend.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerProfileResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
}
