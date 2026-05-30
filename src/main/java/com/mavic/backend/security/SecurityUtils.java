package com.mavic.backend.security;

import com.mavic.backend.model.User;
import com.mavic.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Get the ID of the currently authenticated user
     * @return User ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if the current user has a specific role
     * @param role Role to check (without ROLE_ prefix)
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if the current user is the owner of a customer profile
     * @param customerId Customer ID to check
     * @return true if current user owns this customer profile
     */
    public boolean isCustomerOwner(Long customerId) {
        User currentUser = getCurrentUser();
        return currentUser.getCustomerId() != null && 
               currentUser.getCustomerId().equals(customerId);
    }

    /**
     * Check if the current user is admin of a restaurant
     * @param restaurantId Restaurant ID to check
     * @return true if current user is admin of this restaurant
     */
    public boolean isRestaurantAdmin(Long restaurantId) {
        User currentUser = getCurrentUser();
        return currentUser.getRestaurantId() != null && 
               currentUser.getRestaurantId().equals(restaurantId);
    }

    /**
     * Check if the current user is kitchen staff of a restaurant
     * @param restaurantId Restaurant ID to check
     * @return true if current user is kitchen staff of this restaurant
     */
    public boolean isKitchenStaff(Long restaurantId) {
        User currentUser = getCurrentUser();
        return hasRole("KITCHEN_STAFF") && 
               currentUser.getRestaurantId() != null && 
               currentUser.getRestaurantId().equals(restaurantId);
    }
}
