package com.mavic.backend.repository;

import com.mavic.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findCustomerByPhone(String phone);

    Optional<Customer> findCustomerById(Long id);

    @Query(value = "SELECT c FROM  Customer c where c.id =  :c_id")
    Optional<Customer> getCustomersById(@Param("c_id") Long id);

    Optional<List<Customer>> findCustomerByAddress(String address);
}
