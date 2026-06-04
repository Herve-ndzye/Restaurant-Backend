package com.mavic.backend.order.service;

import com.mavic.backend.order.dto.OrderRequest;
import com.mavic.backend.order.exception.OrderException;
import com.mavic.backend.order.model.Order;
import com.mavic.backend.order.model.Orderitem;
import com.mavic.backend.order.repository.OrderRepository;
import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.repository.CustomerRepository;
import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import com.mavic.backend.restaurant.repository.MenuRepository;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.common.enums.OrderStatus;
import com.mavic.backend.common.security.AuditLog;
import com.mavic.backend.common.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final SecurityUtils securityUtils;

    @AuditLog(action = "PLACE_ORDER")
    @Transactional
    public Order placeOrder(OrderRequest request) {
        // Validate customer ownership
        if (!securityUtils.isCustomerOwner(request.getCustomerId())) {
            throw new AccessDeniedException("You can only place orders for your own account");
        }
        
        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new OrderException("Customer not found"));

        // Validate restaurant
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new OrderException("Restaurant not found"));

        if (!restaurant.getIsOpen()) {
            throw new OrderException("Restaurant is currently closed");
        }

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.ZERO);

        // Save order first to get ID
        order = orderRepository.save(order);

        // Process order items
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Menuitem menuItem = menuRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new OrderException("Menu item not found: " + itemRequest.getMenuItemId()));

            // Validate menu item belongs to restaurant
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new OrderException("Menu item does not belong to the selected restaurant");
            }

            // Validate menu item is available
            if (!menuItem.getIsAvailable()) {
                throw new OrderException("Menu item is not available: " + menuItem.getName());
            }

            // Validate quantity
            if (itemRequest.getQuantity() < 1) {
                throw new OrderException("Quantity must be at least 1");
            }

            // Create order item
            Orderitem orderItem = new Orderitem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            
            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(subtotal);
            
            order.getOrderitems().add(orderItem);
            totalPrice = totalPrice.add(subtotal);
        }

        // Update total price
        order.setTotalPrice(totalPrice);
        
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found"));
        
        // Validate ownership: customer can view their own orders, kitchen staff can view restaurant orders
        if (securityUtils.hasRole("CUSTOMER")) {
            if (!securityUtils.isCustomerOwner(order.getCustomer().getId())) {
                throw new AccessDeniedException("You can only view your own orders");
            }
        } else if (securityUtils.hasRole("KITCHEN_STAFF") || securityUtils.hasRole("RESTAURANT_ADMIN")) {
            if (!securityUtils.isRestaurantAdmin(order.getRestaurant().getId()) && 
                !securityUtils.isKitchenStaff(order.getRestaurant().getId())) {
                throw new AccessDeniedException("You can only view orders for your restaurant");
            }
        }
        
        return order;
    }

    public List<Order> getCustomerOrders(Long customerId) {
        // Validate ownership: customers can only view their own orders
        if (!securityUtils.isCustomerOwner(customerId)) {
            throw new AccessDeniedException("You can only view your own orders");
        }
        
        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new OrderException("Customer not found");
        }
        return orderRepository.findByCustomerIdOrderByCreateAtDesc(customerId);
    }

    @AuditLog(action = "CANCEL_ORDER")
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found"));
        
        // Validate ownership: customers can only cancel their own orders
        if (!securityUtils.isCustomerOwner(order.getCustomer().getId())) {
            throw new AccessDeniedException("You can only cancel your own orders");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // Kitchen operations
    public List<Order> getPendingOrders(Long restaurantId) {
        // Validate ownership: kitchen staff can only view orders for their restaurant
        if (restaurantId != null) {
            if (!securityUtils.isRestaurantAdmin(restaurantId) && 
                !securityUtils.isKitchenStaff(restaurantId)) {
                throw new AccessDeniedException("You can only view orders for your restaurant");
            }
            return orderRepository.findByRestaurantIdAndStatusOrderByCreateAtAsc(restaurantId, OrderStatus.PENDING);
        }
        return orderRepository.findByStatusOrderByCreateAtAsc(OrderStatus.PENDING);
    }

    @AuditLog(action = "ACCEPT_ORDER")
    @Transactional
    public Order acceptOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found"));
        
        // Validate ownership: kitchen staff can only accept orders for their restaurant
        if (!securityUtils.isRestaurantAdmin(order.getRestaurant().getId()) && 
            !securityUtils.isKitchenStaff(order.getRestaurant().getId())) {
            throw new AccessDeniedException("You can only accept orders for your restaurant");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be accepted. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);
        
        // Automatically transition to PREPARING
        order.setStatus(OrderStatus.PREPARING);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markOrderReady(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found"));
        
        // Validate ownership: kitchen staff can only mark orders ready for their restaurant
        if (!securityUtils.isRestaurantAdmin(order.getRestaurant().getId()) && 
            !securityUtils.isKitchenStaff(order.getRestaurant().getId())) {
            throw new AccessDeniedException("You can only mark orders ready for your restaurant");
        }
        
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new OrderException("Only PREPARING orders can be marked as READY. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.READY);
        return orderRepository.save(order);
    }

    @AuditLog(action = "REJECT_ORDER")
    @Transactional
    public Order rejectOrder(Long id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found"));
        
        // Validate ownership: kitchen staff can only reject orders for their restaurant
        if (!securityUtils.isRestaurantAdmin(order.getRestaurant().getId()) && 
            !securityUtils.isKitchenStaff(order.getRestaurant().getId())) {
            throw new AccessDeniedException("You can only reject orders for your restaurant");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be rejected. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason(reason);
        return orderRepository.save(order);
    }

    // Delivery operations
    @Transactional
    public Order markOrderPickedUp(Long id) {
        Order order = getOrderById(id);
        
        if (order.getStatus() != OrderStatus.READY) {
            throw new OrderException("Only READY orders can be picked up. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.PICKED_UP);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markOrderDelivered(Long id) {
        Order order = getOrderById(id);
        
        if (order.getStatus() != OrderStatus.PICKED_UP) {
            throw new OrderException("Only PICKED_UP orders can be delivered. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    public OrderStatus getOrderStatus(Long id) {
        Order order = getOrderById(id);
        return order.getStatus();
    }
}
