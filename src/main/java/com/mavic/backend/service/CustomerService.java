package com.mavic.backend.service;

import com.mavic.backend.controller.CustomerMapper;
import com.mavic.backend.dto.NewCustomerDto;
import com.mavic.backend.dto.ProfileUpdateDto;
import com.mavic.backend.exception.CustomerException;
import com.mavic.backend.model.Customer;
import com.mavic.backend.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CustomerService {

    private CustomerMapper customerMapper;
    private CustomerRepository customerRepository;

    public Customer register(NewCustomerDto customer) {
        if(customerRepository.findCustomerByPhone(customer.getPhone()).isPresent()) throw new CustomerException("Customer already exist");
        var newCustomer = customerMapper.toCustomer(customer);
        customerRepository.save(newCustomer);
        return newCustomer;
    }

    public Customer getCustomer(Long id) {
        var customer = customerRepository.findCustomerById(id).orElse(null);
        if(customer == null ) throw new CustomerException("Customer does not Exist");
        return customer;
    }

    public Customer updateCustomer(Long id, ProfileUpdateDto profile) {
        var customer = customerRepository.getCustomersById(id).orElse(null);
        if(customer == null) throw new CustomerException("Customer does not Exist");
        if(profile.getPhone() == null && profile.getAddress() != null){
            customer.setAddress(profile.getAddress());
            customerRepository.save(customer);
        }else if(profile.getPhone() != null && profile.getAddress() == null){
            customer.setPhone(profile.getPhone());
            customerRepository.save(customer);
        }else{
            customer.setPhone(profile.getPhone());
            customer.setAddress(profile.getAddress());
            customerRepository.save(customer);
        }
        return customer;
    }

    public void deleteCustomer(Long id) {
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
