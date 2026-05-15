package com.mavic.backend.Service;

import com.mavic.backend.controller.CustomerMapper;
import com.mavic.backend.dto.newCustomerDto;
import com.mavic.backend.dto.profileUpdateDto;
import com.mavic.backend.model.Customer;
import com.mavic.backend.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    public Customer register(newCustomerDto customer) {
        if(customerRepository.findCustomerByPhone(customer.getPhone()).isPresent()) throw new CustomerAlreadyExists();
        var newCustomer = customerMapper.toCustomer(customer);
        customerRepository.save(newCustomer);
        return newCustomer;
    }

    public Customer getCustomer(Long id) {
        var customer = customerRepository.findCustomerById(id).orElse(null);
        if(customer == null ) throw new CustomerNotExist();
        return customer;
    }

    public Customer updateCustomer(Long id, profileUpdateDto profile) {
        var customer = customerRepository.getCustomersById(id).orElse(null);
        if(customer == null) throw new CustomerNotExist();
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
        if(customer == null) throw new CustomerNotExist();
        customerRepository.delete(customer);
    }

    public Page<Customer> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("name").ascending()
        );
        var customers = customerRepository.findAll(pageable);
        if(customers.isEmpty()) throw new NoCustomers();
        return customers;
    }
}
