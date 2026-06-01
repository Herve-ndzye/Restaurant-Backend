package com.mavic.backend.service;

import com.mavic.backend.controller.CustomerMapper;
import com.mavic.backend.dto.NewCustomerDto;
import com.mavic.backend.dto.ProfileUpdateDto;
import com.mavic.backend.exception.CustomerException;
import com.mavic.backend.model.Customer;
import com.mavic.backend.model.User;
import com.mavic.backend.repository.CustomerRepository;
import com.mavic.backend.repository.UserRepository;
import com.mavic.backend.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setPhone("1234567890");
        customer.setAddress("123 Main St");

        user = new User();
        user.setId(1L);
        user.setUsername("customer1");
    }

    @Test
    void register_Success() {
        NewCustomerDto dto = new NewCustomerDto("John Doe", "1234567890", "123 Main St");
        when(customerRepository.findCustomerByPhone(dto.getPhone())).thenReturn(Optional.empty());
        when(customerMapper.toCustomer(dto)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        Customer result = customerService.register(dto);

        assertNotNull(result);
        assertEquals(customer.getId(), result.getId());
        assertEquals(customer.getName(), result.getName());
        verify(customerRepository).save(any(Customer.class));
        verify(userRepository).save(user);
        assertEquals(1L, user.getCustomerId());
    }

    @Test
    void register_DuplicatePhone_ThrowsCustomerException() {
        NewCustomerDto dto = new NewCustomerDto("John Doe", "1234567890", "123 Main St");
        when(customerRepository.findCustomerByPhone(dto.getPhone())).thenReturn(Optional.of(customer));

        assertThrows(CustomerException.class, () -> customerService.register(dto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomer_Success() {
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.findCustomerById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCustomer_NotOwner_ThrowsAccessDeniedException() {
        when(securityUtils.isCustomerOwner(1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> customerService.getCustomer(1L));
    }

    @Test
    void getCustomer_NotFound_ThrowsCustomerException() {
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.findCustomerById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerException.class, () -> customerService.getCustomer(1L));
    }

    @Test
    void updateCustomer_Success() {
        ProfileUpdateDto updateDto = new ProfileUpdateDto("0987654321", "456 Oak St");
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.getCustomersById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.updateCustomer(1L, updateDto);

        assertNotNull(result);
        assertEquals("0987654321", result.getPhone());
        assertEquals("456 Oak St", result.getAddress());
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_Partial_Success() {
        ProfileUpdateDto updateDto = new ProfileUpdateDto(null, "456 Oak St");
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.getCustomersById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.updateCustomer(1L, updateDto);

        assertNotNull(result);
        assertEquals("1234567890", result.getPhone()); // Unchanged
        assertEquals("456 Oak St", result.getAddress()); // Updated
        verify(customerRepository).save(customer);
    }

    @Test
    void deleteCustomer_Success() {
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.getCustomersById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }
}
