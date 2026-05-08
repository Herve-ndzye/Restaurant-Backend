package com.mavic.backend.repository;

import com.mavic.backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findCustomerByPhone(String phone);

    Optional<Customer> findCustomerById(Long id);

    Optional<Customer> getCustomersById(Long id);
}
