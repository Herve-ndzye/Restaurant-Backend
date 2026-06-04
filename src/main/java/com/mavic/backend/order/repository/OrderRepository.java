package com.mavic.backend.order.repository;

import com.mavic.backend.order.model.Order;
import com.mavic.backend.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreateAtDesc(Long customerId);
    List<Order> findByStatusOrderByCreateAtAsc(OrderStatus status);
    List<Order> findByRestaurantIdAndStatusOrderByCreateAtAsc(Long restaurantId, OrderStatus status);
}
