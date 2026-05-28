package com.mavic.backend.controller;

import com.mavic.backend.model.Customer;
import com.mavic.backend.dto.NewCustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toCustomer(NewCustomerDto customer);
}
