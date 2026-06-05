package com.mavic.backend.restaurant.controller;

import com.mavic.backend.restaurant.dto.NewMenuItemDto;
import com.mavic.backend.restaurant.exception.RestaurantException;
import com.mavic.backend.restaurant.model.Menuitem;
import com.mavic.backend.restaurant.model.Restaurant;
import com.mavic.backend.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "6. Public - Restaurants", description = "Browse restaurants and menus (public access)")
public class RestaurantController {
    private RestaurantService restaurantService;

    @Operation(
            summary = "Get all restaurants",
            description = "Retrieve list of all open restaurants. Optionally filter by cuisine type. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No restaurants found")
    })
    @GetMapping()
    public ResponseEntity<?> getRestaurants(
            @Parameter(description = "Filter by cuisine type (e.g., Italian, Chinese, Mexican)")
            @RequestParam(required = false) String cuisine
    ){
        var response = restaurantService.getRestaurants(cuisine);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get restaurant by ID",
            description = "Retrieve detailed information about a specific restaurant. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurant found",
                    content = @Content(schema = @Schema(implementation = Restaurant.class))
            ),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurantById(
            @Parameter(description = "Restaurant ID", example = "1")
            @PathVariable("id") Long id
    ){
        var response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get restaurant menu",
            description = "Retrieve all available menu items for a specific restaurant. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Menuitem.class))
            ),
            @ApiResponse(responseCode = "404", description = "Restaurant or menu not found")
    })
    @GetMapping("/{id}/menu")
    public ResponseEntity<?> getMenuByRestaurantId(
            @Parameter(description = "Restaurant ID", example = "1")
            @PathVariable("id") Long id
    ){
        var response = restaurantService.getRestaurantMenu(id);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RestaurantException.class)
    public ResponseEntity<?> handleRestaurantException(RestaurantException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", ex.getMessage()));
    }
}
