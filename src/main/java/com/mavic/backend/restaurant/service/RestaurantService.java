package com.mavic.backend.restaurant.service;

import com.mavic.backend.common.enums.Category;
import com.mavic.backend.common.security.AuditLog;
import com.mavic.backend.common.security.SecurityUtils;
import com.mavic.backend.common.util.PaginationUtils;
import com.mavic.backend.restaurant.dto.CreateMenuItemRequest;
import com.mavic.backend.restaurant.dto.UpdateMenuItemRequest;
import com.mavic.backend.restaurant.exception.RestaurantException;
import com.mavic.backend.restaurant.mapper.RestaurantMapper;
import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.restaurant.repository.MenuRepository;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class RestaurantService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final SecurityUtils securityUtils;

    @Cacheable(value = "restaurants", key = "#cuisine + '-' + #page + '-' + #size")
    public Page<Restaurant> getRestaurants(String cuisine, int page, int size) {
        Pageable pageable = PaginationUtils.createPageable(page, size, "name");
        if (cuisine == null) {
            return restaurantRepository.findByIsOpen(Boolean.TRUE, pageable);
        }
        return restaurantRepository.findAllByCuisine(cuisine, pageable);
    }

    @Cacheable(value = "restaurant", key = "#id")
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantException("Restaurant with ID " + id + " was not found."));
    }

    @Cacheable(value = "restaurantMenu", key = "#id")
    public List<Menuitem> getRestaurantMenu(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RestaurantException("Restaurant with ID " + id + " was not found.");
        }
        return menuRepository.findByRestaurantIdAndIsAvailable(id, true);
    }

    @AuditLog(action = "ADD_MENU_ITEM")
    @Transactional
    @CacheEvict(value = "restaurantMenu", key = "#id")
    public void addMenuItem(Long id, CreateMenuItemRequest menuItem) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantException("Restaurant with ID " + id + " was not found."));

        if (!securityUtils.isRestaurantAdmin(id)) {
            throw new AccessDeniedException("You can only add menu items to your own restaurant.");
        }
        validateCategory(menuItem.getCategory());
        menuRepository.save(restaurantMapper.toMenuItem(menuItem, restaurant));
    }

    @AuditLog(action = "UPDATE_MENU_ITEM")
    @Transactional
    @CacheEvict(value = "restaurantMenu", allEntries = true)
    public void updateMenuItem(Long id, UpdateMenuItemRequest menuItem) {
        Menuitem existingMenuItem = menuRepository.findById(id)
                .orElseThrow(() -> new RestaurantException("Menu item with ID " + id + " was not found."));

        if (!securityUtils.isRestaurantAdmin(existingMenuItem.getRestaurant().getId())) {
            throw new AccessDeniedException("You can only update menu items for your own restaurant.");
        }

        if (menuItem.getName() != null) {
            existingMenuItem.setName(menuItem.getName());
        }
        if (menuItem.getDescription() != null) {
            existingMenuItem.setDescription(menuItem.getDescription());
        }
        if (menuItem.getPrice() != null) {
            if (menuItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RestaurantException("Menu item price must be greater than zero.", HttpStatus.BAD_REQUEST);
            }
            existingMenuItem.setPrice(menuItem.getPrice());
        }
        if (menuItem.getCategory() != null) {
            validateCategory(menuItem.getCategory());
            existingMenuItem.setCategory(Category.valueOf(menuItem.getCategory().toUpperCase()));
        }

        menuRepository.save(existingMenuItem);
    }

    @AuditLog(action = "DELETE_MENU_ITEM")
    @Transactional
    @CacheEvict(value = "restaurantMenu", allEntries = true)
    public void deleteMenuItem(Long id) {
        Menuitem menuItem = menuRepository.findById(id)
                .orElseThrow(() -> new RestaurantException("Menu item with ID " + id + " was not found."));

        if (!securityUtils.isRestaurantAdmin(menuItem.getRestaurant().getId())) {
            throw new AccessDeniedException("You can only delete menu items from your own restaurant.");
        }

        menuItem.setIsAvailable(false);
        menuRepository.save(menuItem);
    }

    private void validateCategory(String category) {
        if (!Category.exists(category)) {
            throw new RestaurantException(
                    "Invalid category '" + category + "'. Valid categories are: STARTER, MAIN, DESSERT, DRINK, SIDE.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
