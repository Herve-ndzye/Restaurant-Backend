package com.mavic.backend.service;

import com.mavic.backend.controller.RestaurantMapper;
import com.mavic.backend.dto.NewMenuItemDto;
import com.mavic.backend.exception.RestaurantException;
import com.mavic.backend.model.Menuitem;
import com.mavic.backend.model.Restaurant;
import com.mavic.backend.model.enums.Category;
import com.mavic.backend.repository.MenuRepository;
import com.mavic.backend.repository.RestaurantRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class RestaurantService {
    private final MenuRepository menuRepository;
    private RestaurantRepository restaurantRepository;
    private RestaurantMapper restaurantMapper;

    public List<Restaurant> getRestaurants(String cuisine) {
        if(cuisine == null){
            var restaurants = restaurantRepository.findByIsOpen(Boolean.TRUE);
            if(restaurants.isEmpty()){
                throw new RestaurantException("Restaurant not found");
            }
            return restaurants;
        }
        return restaurantRepository.findAllByCuisine(cuisine);
    }

    public Restaurant getRestaurantById(Long id) {
        var restaurant = restaurantRepository.findById(id).orElse(null);
        if(restaurant == null){
            throw new RestaurantException("Restaurant not found");
        }
        return restaurant;
    }

    public List<Menuitem> getRestaurantMenu(Long id) {
        var restaurant = restaurantRepository.findById(id).orElse(null);
        if(restaurant == null){
            throw new RestaurantException("Restaurant not found");
        }
        var menu = menuRepository.findByRestaurantIdAndIsAvailable(id,true);
        if(menu == null) {
            throw new RestaurantException("Menu not found");
        }
        return menu;
    }

    public void addMenuItem(Long id, @Valid NewMenuItemDto menuItem) {
        var restaurant = restaurantRepository.findById(id).orElse(null);
        if(restaurant == null){
            throw new RestaurantException("Restaurant not found");
        }
        if(!Category.exists(menuItem.getCategory())){
            throw new RestaurantException("Category does not exist");
        }
        menuRepository.save(restaurantMapper.toMenuItem(menuItem, restaurant));
    }

    public void updateMenuItem(Long id, @Valid NewMenuItemDto menuItem) {
        var existingMenuItem = menuRepository.findById(id).orElse(null);
        if(existingMenuItem == null){
            throw new RestaurantException("Menu item not found");
        }
        
        // Update fields if provided
        if(menuItem.getName() != null){
            existingMenuItem.setName(menuItem.getName());
        }
        if(menuItem.getDescription() != null){
            existingMenuItem.setDescription(menuItem.getDescription());
        }
        if(menuItem.getPrice() != null){
            if(menuItem.getPrice().compareTo(BigDecimal.ZERO) <= 0){
                throw new RestaurantException("Price must be > 0");
            }
            existingMenuItem.setPrice(menuItem.getPrice());
        }
        if(menuItem.getCategory() != null){
            if(!Category.exists(menuItem.getCategory())){
                throw new RestaurantException("Category does not exist");
            }
            existingMenuItem.setCategory(Category.valueOf(menuItem.getCategory()));
        }
        
        menuRepository.save(existingMenuItem);
    }

    public void deleteMenuItem(Long id) {
        var menuItem = menuRepository.findById(id).orElse(null);
        if(menuItem == null){
            throw new RestaurantException("Menu item not found");
        }
        
        // Soft delete: set isAvailable to false
        menuItem.setIsAvailable(false);
        menuRepository.save(menuItem);
    }
}
