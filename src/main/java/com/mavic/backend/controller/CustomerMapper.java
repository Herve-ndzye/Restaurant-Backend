package com.mavic.backend.controller;

import com.mavic.backend.model.Customer;
import com.mavic.backend.dto.newCustomerDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(newCustomerDto customer);
}
