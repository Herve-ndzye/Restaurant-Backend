package com.mavic.backend.service;

import com.mavic.backend.common.enums.Category;
import com.mavic.backend.common.security.SecurityUtils;
import com.mavic.backend.restaurant.dto.CreateMenuItemRequest;
import com.mavic.backend.restaurant.dto.UpdateMenuItemRequest;
import com.mavic.backend.restaurant.exception.RestaurantException;
import com.mavic.backend.restaurant.mapper.RestaurantMapper;
import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.restaurant.repository.MenuRepository;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import com.mavic.backend.restaurant.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantMapper restaurantMapper;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;
    private Menuitem menuItem;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Bella Italia");
        restaurant.setCuisine("Italian");
        restaurant.setIsOpen(true);

        menuItem = new Menuitem();
        menuItem.setId(10L);
        menuItem.setName("Pizza");
        menuItem.setPrice(BigDecimal.valueOf(12.99));
        menuItem.setCategory(Category.MAIN);
        menuItem.setIsAvailable(true);
        menuItem.setRestaurant(restaurant);
    }

    @Test
    void getRestaurants_All_Success() {
        when(restaurantRepository.findByIsOpen(true, any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(restaurant)));

        Page<Restaurant> result = restaurantService.getRestaurants(null, 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRestaurantById_NotFound_ThrowsException() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RestaurantException.class, () -> restaurantService.getRestaurantById(1L));
    }

    @Test
    void addMenuItem_Success() {
        CreateMenuItemRequest dto = new CreateMenuItemRequest();
        dto.setName("Pasta");
        dto.setPrice(BigDecimal.valueOf(14.99));
        dto.setCategory("MAIN");

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(securityUtils.isRestaurantAdmin(1L)).thenReturn(true);
        when(restaurantMapper.toMenuItem(dto, restaurant)).thenReturn(menuItem);

        restaurantService.addMenuItem(1L, dto);

        verify(menuRepository).save(any(Menuitem.class));
    }

    @Test
    void addMenuItem_NotAdmin_ThrowsAccessDeniedException() {
        CreateMenuItemRequest dto = new CreateMenuItemRequest();
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(securityUtils.isRestaurantAdmin(1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> restaurantService.addMenuItem(1L, dto));
    }

    @Test
    void updateMenuItem_Success() {
        UpdateMenuItemRequest dto = new UpdateMenuItemRequest();
        dto.setName("New Pizza Name");
        dto.setPrice(BigDecimal.valueOf(15.99));

        when(menuRepository.findById(10L)).thenReturn(Optional.of(menuItem));
        when(securityUtils.isRestaurantAdmin(1L)).thenReturn(true);

        restaurantService.updateMenuItem(10L, dto);

        assertEquals("New Pizza Name", menuItem.getName());
        verify(menuRepository).save(menuItem);
    }
}
