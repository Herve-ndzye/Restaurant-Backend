package com.mavic.backend.service;

import com.mavic.backend.order.dto.OrderRequest;
import com.mavic.backend.order.exception.OrderException;
import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.order.model.Order;
import com.mavic.backend.order.model.Orderitem;
import com.mavic.backend.common.enums.Category;
import com.mavic.backend.common.enums.OrderStatus;
import com.mavic.backend.order.repository.OrderRepository;
import com.mavic.backend.customer.repository.CustomerRepository;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import com.mavic.backend.restaurant.repository.MenuRepository;
import com.mavic.backend.common.security.SecurityUtils;
import com.mavic.backend.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Restaurant restaurant;
    private Menuitem menuItem;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        restaurant = new Restaurant();
        restaurant.setId(2L);
        restaurant.setName("Bella Italia");
        restaurant.setIsOpen(true);

        menuItem = new Menuitem();
        menuItem.setId(3L);
        menuItem.setName("Pasta");
        menuItem.setPrice(BigDecimal.valueOf(10.00));
        menuItem.setIsAvailable(true);
        menuItem.setRestaurant(restaurant);
        menuItem.setCategory(Category.MAIN);

        order = new Order();
        order.setId(100L);
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.valueOf(20.00));
        order.setOrderitems(new java.util.LinkedHashSet<>());
    }

    @Test
    void placeOrder_Success() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(1L);
        request.setRestaurantId(2L);

        OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
        itemRequest.setMenuItemId(3L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant));
        when(menuRepository.findById(3L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(request);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(BigDecimal.valueOf(20.00), result.getTotalPrice());
        assertEquals(1, result.getOrderitems().size());
        assertEquals(2, result.getOrderitems().iterator().next().getQuantity());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void placeOrder_NotOwner_ThrowsAccessDeniedException() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(1L);
        when(securityUtils.isCustomerOwner(1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void placeOrder_ClosedRestaurant_ThrowsOrderException() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(1L);
        request.setRestaurantId(2L);
        restaurant.setIsOpen(false);

        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant));

        assertThrows(OrderException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(true);
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);

        Order result = orderService.getOrderById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getOrderById_AccessDenied() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(true);
        when(securityUtils.isCustomerOwner(1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> orderService.getOrderById(100L));
    }

    @Test
    void cancelOrder_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);

        orderService.cancelOrder(100L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_NotPending_ThrowsOrderException() {
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.isCustomerOwner(1L)).thenReturn(true);

        assertThrows(OrderException.class, () -> orderService.cancelOrder(100L));
    }

    @Test
    void acceptOrder_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.isRestaurantAdmin(2L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.acceptOrder(100L);

        assertEquals(OrderStatus.PREPARING, result.getStatus());
        verify(orderRepository, times(2)).save(order);
    }

    @Test
    void markOrderReady_Success() {
        order.setStatus(OrderStatus.PREPARING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.isRestaurantAdmin(2L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.markOrderReady(100L);

        assertEquals(OrderStatus.READY, result.getStatus());
    }

    @Test
    void rejectOrder_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.isRestaurantAdmin(2L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.rejectOrder(100L, "Out of ingredients");

        assertEquals(OrderStatus.REJECTED, result.getStatus());
        assertEquals("Out of ingredients", result.getRejectionReason());
    }

    @Test
    void markOrderPickedUp_Success() {
        order.setStatus(OrderStatus.READY);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.markOrderPickedUp(100L);

        assertEquals(OrderStatus.PICKED_UP, result.getStatus());
    }

    @Test
    void markOrderDelivered_Success() {
        order.setStatus(OrderStatus.PICKED_UP);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.markOrderDelivered(100L);

        assertEquals(OrderStatus.DELIVERED, result.getStatus());
    }
}
