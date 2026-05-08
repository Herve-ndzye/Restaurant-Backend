package com.mavic.backend.controller;

import com.mavic.backend.dto.newCustomerDto;
import com.mavic.backend.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/customer")
public class CustomerController {
    private CustomerRepository customerRepository;
    private CustomerMapper customerMapper;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @RequestBody newCustomerDto customer,
            UriComponentsBuilder uriBuilder
    ){
        if(customerRepository.findCustomerByPhone(customer.getPhone()).isPresent()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of("Error : ","Customer already Exists")
                    );
        }
        var newCustomer = customerMapper.toCustomer(customer);
        customerRepository.save(newCustomer);
        var uri =uriBuilder.path("/customers/{id}").buildAndExpand(newCustomer.getId()).toUri();
        return ResponseEntity.created(uri).body(newCustomer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable("id") Long id
    ){
        var customer = customerRepository.findCustomerById(id).orElse(null);
        if(customer == null ) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of("Error : ", "Customer does not Exist")
                    );
        }
        return ResponseEntity
                .status(200)
                .body(customer);
    }
}
