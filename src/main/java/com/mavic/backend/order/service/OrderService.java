package com.mavic.backend.order.service;

import com.mavic.backend.auth.model.User;
import com.mavic.backend.common.enums.OrderStatus;
import com.mavic.backend.common.security.AuditLog;
import com.mavic.backend.common.security.SecurityUtils;
import com.mavic.backend.common.util.PaginationUtils;
import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.repository.CustomerRepository;
import com.mavic.backend.order.dto.OrderRequest;
import com.mavic.backend.order.exception.OrderException;
import com.mavic.backend.order.model.Order;
import com.mavic.backend.order.model.Orderitem;
import com.mavic.backend.order.repository.OrderRepository;
import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.restaurant.repository.MenuRepository;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        if (!securityUtils.isCustomerOwner(request.getCustomerId())) {
            throw new AccessDeniedException("You can only place orders for your own account.");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new OrderException("Customer with ID " + request.getCustomerId() + " was not found.", HttpStatus.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new OrderException("Restaurant with ID " + request.getRestaurantId() + " was not found.", HttpStatus.NOT_FOUND));

        if (!restaurant.getIsOpen()) {
            throw new OrderException("Restaurant '" + restaurant.getName() + "' is currently closed.");
        }

        List<Long> menuItemIds = request.getItems().stream()
                .map(OrderRequest.OrderItemRequest::getMenuItemId)
                .toList();
        Map<Long, Menuitem> menuItemsById = menuRepository.findAllById(menuItemIds).stream()
                .collect(Collectors.toMap(Menuitem::getId, Function.identity()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.ZERO);
        order = orderRepository.save(order);

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Menuitem menuItem = menuItemsById.get(itemRequest.getMenuItemId());
            if (menuItem == null) {
                throw new OrderException("Menu item with ID " + itemRequest.getMenuItemId() + " was not found.", HttpStatus.NOT_FOUND);
            }
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new OrderException("Menu item '" + menuItem.getName() + "' does not belong to the selected restaurant.");
            }
            if (!menuItem.getIsAvailable()) {
                throw new OrderException("Menu item '" + menuItem.getName() + "' is not available.");
            }
            if (itemRequest.getQuantity() < 1) {
                throw new OrderException("Quantity must be at least 1 for each menu item.");
            }

            Orderitem orderItem = new Orderitem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(subtotal);

            order.getOrderitems().add(orderItem);
            totalPrice = totalPrice.add(subtotal);
        }

        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new OrderException("Order with ID " + id + " was not found.", HttpStatus.NOT_FOUND));

        assertCanViewOrder(order);
        return order;
    }

    public Page<Order> getCustomerOrders(Long customerId, int page, int size) {
        if (!securityUtils.isCustomerOwner(customerId)) {
            throw new AccessDeniedException("You can only view your own orders.");
        }
        if (!customerRepository.existsById(customerId)) {
            throw new OrderException("Customer with ID " + customerId + " was not found.", HttpStatus.NOT_FOUND);
        }

        Pageable pageable = PaginationUtils.createPageable(page, size);
        return orderRepository.findByCustomerIdWithDetails(customerId, pageable);
    }

    @AuditLog(action = "CANCEL_ORDER")
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order with ID " + id + " was not found.", HttpStatus.NOT_FOUND));

        if (!securityUtils.isCustomerOwner(order.getCustomer().getId())) {
            throw new AccessDeniedException("You can only cancel your own orders.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public Page<Order> getPendingOrders(Long restaurantId, int page, int size) {
        User currentUser = securityUtils.getCurrentUser();
        Long effectiveRestaurantId = restaurantId != null ? restaurantId : currentUser.getRestaurantId();

        if (effectiveRestaurantId == null) {
            throw new AccessDeniedException("Restaurant ID is required to view pending orders.");
        }
        if (!securityUtils.isRestaurantAdmin(effectiveRestaurantId) &&
                !securityUtils.isKitchenStaff(effectiveRestaurantId)) {
            throw new AccessDeniedException("You can only view orders for your own restaurant.");
        }
        if (restaurantId != null && !restaurantId.equals(currentUser.getRestaurantId()) &&
                !securityUtils.hasRole("RESTAURANT_ADMIN")) {
            throw new AccessDeniedException("You can only view orders for your own restaurant.");
        }

        Pageable pageable = PaginationUtils.createPageable(page, size, "createAt");
        return orderRepository.findPendingByRestaurantWithDetails(
                effectiveRestaurantId,
                OrderStatus.PENDING,
                pageable
        );
    }

    @AuditLog(action = "ACCEPT_ORDER")
    @Transactional
    public Order acceptOrder(Long id) {
        Order order = loadKitchenOrder(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be accepted. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);
        order.setStatus(OrderStatus.PREPARING);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markOrderReady(Long id) {
        Order order = loadKitchenOrder(id);
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new OrderException("Only PREPARING orders can be marked as READY. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.READY);
        return orderRepository.save(order);
    }

    @AuditLog(action = "REJECT_ORDER")
    @Transactional
    public Order rejectOrder(Long id, String reason) {
        Order order = loadKitchenOrder(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be rejected. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason(reason);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markOrderPickedUp(Long id) {
        assertDeliveryDriver();
        Order order = loadDeliveryOrder(id);
        if (order.getStatus() != OrderStatus.READY) {
            throw new OrderException("Only READY orders can be picked up. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PICKED_UP);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markOrderDelivered(Long id) {
        assertDeliveryDriver();
        Order order = loadDeliveryOrder(id);
        if (order.getStatus() != OrderStatus.PICKED_UP) {
            throw new OrderException("Only PICKED_UP orders can be delivered. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    public OrderStatus getOrderStatus(Long id) {
        return getOrderById(id).getStatus();
    }

    private Order loadKitchenOrder(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new OrderException("Order with ID " + id + " was not found.", HttpStatus.NOT_FOUND));

        if (!securityUtils.isRestaurantAdmin(order.getRestaurant().getId()) &&
                !securityUtils.isKitchenStaff(order.getRestaurant().getId())) {
            throw new AccessDeniedException("You can only manage orders for your own restaurant.");
        }
        return order;
    }

    private Order loadDeliveryOrder(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new OrderException("Order with ID " + id + " was not found.", HttpStatus.NOT_FOUND));
    }

    private void assertDeliveryDriver() {
        if (!securityUtils.hasRole("DELIVERY_DRIVER")) {
            throw new AccessDeniedException("Only delivery drivers can perform this action.");
        }
    }

    private void assertCanViewOrder(Order order) {
        if (securityUtils.hasRole("CUSTOMER")) {
            if (!securityUtils.isCustomerOwner(order.getCustomer().getId())) {
                throw new AccessDeniedException("You can only view your own orders.");
            }
        } else if (securityUtils.hasRole("KITCHEN_STAFF") || securityUtils.hasRole("RESTAURANT_ADMIN")) {
            if (!securityUtils.isRestaurantAdmin(order.getRestaurant().getId()) &&
                    !securityUtils.isKitchenStaff(order.getRestaurant().getId())) {
                throw new AccessDeniedException("You can only view orders for your restaurant.");
            }
        } else if (securityUtils.hasRole("DELIVERY_DRIVER")) {
            if (order.getStatus() != OrderStatus.READY &&
                    order.getStatus() != OrderStatus.PICKED_UP &&
                    order.getStatus() != OrderStatus.DELIVERED) {
                throw new AccessDeniedException("This order is not available for delivery.");
            }
        }
    }
}
