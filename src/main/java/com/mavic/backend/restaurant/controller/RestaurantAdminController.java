package com.mavic.backend.restaurant.controller;

import com.mavic.backend.restaurant.dto.CreateMenuItemRequest;
import com.mavic.backend.restaurant.dto.UpdateMenuItemRequest;
import com.mavic.backend.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/restaurants")
@Tag(name = "5. Admin", description = "Customer management, menu management, staff registration, and admin invitations")
public class RestaurantAdminController {
    private RestaurantService restaurantService;

    @Operation(summary = "Add menu item", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/{id}/menu")
    public ResponseEntity<Map<String, String>> addMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateMenuItemRequest menuItem) {
        restaurantService.addMenuItem(id, menuItem);
        return ResponseEntity.ok(Map.of("message", "Menu item added successfully"));
    }

    @Operation(summary = "Update menu item", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/menu/{id}")
    public ResponseEntity<Map<String, String>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest menuItem) {
        restaurantService.updateMenuItem(id, menuItem);
        return ResponseEntity.ok(Map.of("message", "Menu item updated successfully"));
    }

    @Operation(summary = "Delete menu item", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Map<String, String>> deleteMenuItem(@PathVariable Long id) {
        restaurantService.deleteMenuItem(id);
        return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
    }
}
