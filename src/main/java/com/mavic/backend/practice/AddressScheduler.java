package com.mavic.backend.practice;

import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AddressScheduler {
    private CustomerRepository customerRepository;


    public void notifyArrival(){
        var ruyenziCustomers = customerRepository.findCustomerByAddress("Ruyenzi").orElse(null);
        if(ruyenziCustomers == null){
            System.out.println("No Ruyenzi customers found");
        }else{
            for(Customer c : ruyenziCustomers){
                System.out.println(c.getName()+" you delivery is here.");
            }
        }
    }
}
