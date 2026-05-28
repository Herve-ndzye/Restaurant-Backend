package com.mavic.backend.controller;

import com.mavic.backend.dto.NewCustomerDto;
import com.mavic.backend.model.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:43:23+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public Customer toCustomer(NewCustomerDto customer) {
        if ( customer == null ) {
            return null;
        }

        Customer customer1 = new Customer();

        customer1.setName( customer.getName() );
        customer1.setPhone( customer.getPhone() );
        customer1.setAddress( customer.getAddress() );

        return customer1;
    }
}
