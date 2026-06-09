package com.mavic.backend.restaurant.repository;

import com.mavic.backend.restaurant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Page<Restaurant> findAllByCuisine(String cuisine, Pageable pageable);

    Page<Restaurant> findByIsOpen(Boolean isOpen, Pageable pageable);
}
