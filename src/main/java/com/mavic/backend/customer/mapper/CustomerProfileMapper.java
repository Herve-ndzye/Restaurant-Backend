package com.mavic.backend.customer.mapper;

import com.mavic.backend.customer.dto.CustomerProfileResponse;
import com.mavic.backend.customer.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerProfileMapper {

    public CustomerProfileResponse toResponse(Customer customer) {
        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
