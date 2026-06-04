package com.mavic.backend.customer.mapper;

import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.dto.NewCustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toCustomer(NewCustomerDto customer);
}
