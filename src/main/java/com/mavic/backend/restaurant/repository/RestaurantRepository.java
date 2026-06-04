package com.mavic.backend.restaurant.repository;

import com.mavic.backend.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
    List<Restaurant> findAllByCuisine(String cuisine);

    List<Restaurant> findByIsOpen(Boolean isOpen);
}
