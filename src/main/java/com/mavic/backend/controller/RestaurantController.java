package com.mavic.backend.controller;

import com.mavic.backend.dto.NewMenuItemDto;
import com.mavic.backend.exception.RestaurantException;
import com.mavic.backend.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    private RestaurantService restaurantService;

    @GetMapping()
    public ResponseEntity<?> getRestaurants(
            @RequestParam(required = false) String cuisine
    ){
        var response = restaurantService.getRestaurants(cuisine);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurantById(
            @PathVariable("id") Long id
    ){
        var response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<?> getMenuByRestaurantId(
            @PathVariable("id") Long id
    ){
        var response = restaurantService.getRestaurantMenu(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<?> addMenuItem(
            @PathVariable Long id,
           @Valid @RequestBody NewMenuItemDto menuItem
    ){
        restaurantService.addMenuItem(id,menuItem);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody NewMenuItemDto menuItem
    ){
        restaurantService.updateMenuItem(id, menuItem);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable Long id
    ){
        restaurantService.deleteMenuItem(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(RestaurantException.class)
    public ResponseEntity<?> handleRestaurantException(String message){
        return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "Error", message
                ));
    }
}
