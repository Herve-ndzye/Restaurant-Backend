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
public class RestaurantController {
    private RestaurantService restaurantService;

    @Tag(name = "Restaurants")
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

    @Tag(name = "Restaurants")
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

    @Tag(name = "Restaurants")
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

    @Tag(name = "Menu Management")
    @Operation(
            summary = "Add menu item",
            description = "Add a new menu item to restaurant. Requires RESTAURANT_ADMIN role. Admin can only add items to their own restaurant.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to add items to this restaurant"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PostMapping("/{id}/menu")
    public ResponseEntity<?> addMenuItem(
            @Parameter(description = "Restaurant ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Menu item details",
                    content = @Content(
                            schema = @Schema(implementation = NewMenuItemDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Margherita Pizza",
                                      "description": "Classic pizza with tomato sauce, mozzarella, and basil",
                                      "price": 12.99,
                                      "category": "MAIN_COURSE"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody NewMenuItemDto menuItem
    ){
        restaurantService.addMenuItem(id,menuItem);
        return ResponseEntity.ok().build();
    }

    @Tag(name = "Menu Management")
    @Operation(
            summary = "Update menu item",
            description = "Update existing menu item details. Requires RESTAURANT_ADMIN role. Admin can only update items from their own restaurant.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this menu item"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(
            @Parameter(description = "Menu item ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated menu item details (all fields optional for partial update)",
                    content = @Content(
                            schema = @Schema(implementation = NewMenuItemDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Margherita Pizza (Updated)",
                                      "price": 13.99
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody NewMenuItemDto menuItem
    ){
        restaurantService.updateMenuItem(id, menuItem);
        return ResponseEntity.ok().build();
    }

    @Tag(name = "Menu Management")
    @Operation(
            summary = "Delete menu item",
            description = "Soft delete menu item (sets isAvailable=false). Requires RESTAURANT_ADMIN role. Admin can only delete items from their own restaurant.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu item deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this menu item"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(
            @Parameter(description = "Menu item ID", example = "1")
            @PathVariable Long id
    ){
        restaurantService.deleteMenuItem(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(RestaurantException.class)
    public ResponseEntity<?> handleRestaurantException(RestaurantException ex){
        return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "Error", ex.getMessage()
                ));
    }
}
