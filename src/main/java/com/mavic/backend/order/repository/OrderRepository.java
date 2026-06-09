package com.mavic.backend.order.repository;

import com.mavic.backend.order.model.Order;
import com.mavic.backend.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerIdOrderByCreateAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByRestaurantIdAndStatusOrderByCreateAtAsc(Long restaurantId, OrderStatus status, Pageable pageable);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.customer
            LEFT JOIN FETCH o.restaurant
            LEFT JOIN FETCH o.orderitems oi
            LEFT JOIN FETCH oi.menuItem
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.customer
            LEFT JOIN FETCH o.restaurant
            LEFT JOIN FETCH o.orderitems oi
            LEFT JOIN FETCH oi.menuItem
            WHERE o.customer.id = :customerId
            ORDER BY o.createAt DESC
            """)
    Page<Order> findByCustomerIdWithDetails(@Param("customerId") Long customerId, Pageable pageable);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.customer
            LEFT JOIN FETCH o.restaurant
            LEFT JOIN FETCH o.orderitems oi
            LEFT JOIN FETCH oi.menuItem
            WHERE o.restaurant.id = :restaurantId AND o.status = :status
            ORDER BY o.createAt ASC
            """)
    Page<Order> findPendingByRestaurantWithDetails(
            @Param("restaurantId") Long restaurantId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
