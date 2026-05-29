package com.mavic.backend.controller;

import com.mavic.backend.exception.CustomerException;
import com.mavic.backend.service.CustomerService;
import com.mavic.backend.dto.NewCustomerDto;
import com.mavic.backend.dto.ProfileUpdateDto;
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
    private CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @RequestBody NewCustomerDto customer,
            UriComponentsBuilder uriBuilder
    ){
        var newCustomer = customerService.register(customer);
        var uri =uriBuilder.path("/customers/{id}").buildAndExpand(newCustomer.getId()).toUri();
        return ResponseEntity.created(uri).body(newCustomer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable("id") Long id
    ){
        var customer = customerService.getCustomer(id);
        return ResponseEntity
                .status(200)
                .body(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable("id") Long id,
            @RequestBody ProfileUpdateDto profile
    ){
        var customer = customerService.updateCustomer(id,profile);
        return ResponseEntity
                .status(200)
                .body(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable("id") Long id
    ){
        customerService.deleteCustomer(id);
        return ResponseEntity
                .status(200)
                .body(
                        Map.of("Message", "Customer deleted Successfully.")
                );
    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ){
        var customers = customerService.getAllCustomers(page,size);
        return ResponseEntity
                .ok()
                .body(customers);
    }

    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<?> handleCustomerException(CustomerException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of("Error", ex.getMessage())
                );
    }

}
