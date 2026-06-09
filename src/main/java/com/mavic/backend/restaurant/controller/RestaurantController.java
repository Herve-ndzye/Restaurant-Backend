package com.mavic.backend.restaurant.controller;

import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "6. Public - Restaurants", description = "Browse restaurants and menus (public access)")
public class RestaurantController {
    private RestaurantService restaurantService;

    @Operation(summary = "Get all restaurants (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurants retrieved",
                    content = @Content(schema = @Schema(implementation = Restaurant.class)))
    })
    @GetMapping()
    public ResponseEntity<Page<Restaurant>> getRestaurants(
            @RequestParam(required = false) String cuisine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(restaurantService.getRestaurants(cuisine, page, size));
    }

    @Operation(summary = "Get restaurant by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @Operation(summary = "Get restaurant menu")
    @GetMapping("/{id}/menu")
    public ResponseEntity<List<Menuitem>> getMenuByRestaurantId(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantMenu(id));
    }
}
