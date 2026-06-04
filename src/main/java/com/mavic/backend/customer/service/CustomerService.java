package com.mavic.backend.customer.service;

import com.mavic.backend.customer.mapper.CustomerMapper;
import com.mavic.backend.customer.dto.NewCustomerDto;
import com.mavic.backend.customer.dto.ProfileUpdateDto;
import com.mavic.backend.customer.exception.CustomerException;
import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.repository.CustomerRepository;
import com.mavic.backend.auth.model.User;
import com.mavic.backend.auth.repository.UserRepository;
import com.mavic.backend.common.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    public Customer register(NewCustomerDto customer) {
        if(customerRepository.findCustomerByPhone(customer.getPhone()).isPresent()) throw new CustomerException("Customer already exist");
        var newCustomer = customerMapper.toCustomer(customer);
        customerRepository.save(newCustomer);
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            currentUser.setCustomerId(newCustomer.getId());
            userRepository.save(currentUser);
        } catch (Exception e) {
            // Log or ignore if called out of authenticated context
        }
        
        return newCustomer;
    }

    public Customer getCustomer(Long id) {
        // Validate ownership: customers can only view their own profile
        if (!securityUtils.isCustomerOwner(id)) {
            throw new AccessDeniedException("You can only view your own profile");
        }
        
        var customer = customerRepository.findCustomerById(id).orElse(null);
        if(customer == null ) throw new CustomerException("Customer does not Exist");
        return customer;
    }

    public Customer updateCustomer(Long id, ProfileUpdateDto profile) {
        // Validate ownership: customers can only update their own profile
        if (!securityUtils.isCustomerOwner(id)) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        
        var customer = customerRepository.getCustomersById(id).orElse(null);
        if(customer == null) throw new CustomerException("Customer does not Exist");
        
        boolean updated = false;
        if(profile.getPhone() != null && !profile.getPhone().trim().isEmpty()){
            customer.setPhone(profile.getPhone());
            updated = true;
        }
        if(profile.getAddress() != null && !profile.getAddress().trim().isEmpty()){
            customer.setAddress(profile.getAddress());
            updated = true;
        }
        
        if (updated) {
            customerRepository.save(customer);
        }
        return customer;
    }

    public void deleteCustomer(Long id) {
        // Validate ownership: customers can only delete their own profile
        if (!securityUtils.isCustomerOwner(id)) {
            throw new AccessDeniedException("You can only delete your own profile");
        }
        
        var customer = customerRepository.getCustomersById(id).orElse(null);
        if(customer == null) throw new CustomerException("Customer does not Exist");
        customerRepository.delete(customer);
    }

    public Page<Customer> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("name").ascending()
        );
        var customers = customerRepository.findAll(pageable);
        if(customers.isEmpty()) throw new CustomerException("No Customers Available");
        return customers;
    }
}
