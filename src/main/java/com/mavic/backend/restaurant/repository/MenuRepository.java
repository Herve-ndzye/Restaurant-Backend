package com.mavic.backend.restaurant.repository;

import com.mavic.backend.restaurant.model.Menuitem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menuitem, Long> {
    List<Menuitem> findByRestaurantIdAndIsAvailable(Long restaurantId, Boolean isAvailable);
}
