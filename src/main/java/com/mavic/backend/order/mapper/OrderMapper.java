package com.mavic.backend.order.mapper;

import com.mavic.backend.order.dto.OrderItemResponse;
import com.mavic.backend.order.dto.OrderResponse;
import com.mavic.backend.order.model.Order;
import com.mavic.backend.order.model.Orderitem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .rejectionReason(order.getRejectionReason())
                .createdAt(order.getCreateAt())
                .updatedAt(order.getUpdateAt())
                .items(order.getOrderitems().stream().map(this::toItemResponse).toList())
                .build();
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream().map(this::toResponse).toList();
    }

    private OrderItemResponse toItemResponse(Orderitem item) {
        return OrderItemResponse.builder()
                .menuItemId(item.getMenuItem().getId())
                .menuItemName(item.getMenuItem().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getMenuItem().getPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
