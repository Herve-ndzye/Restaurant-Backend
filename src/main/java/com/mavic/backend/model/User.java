package com.mavic.backend.model;

import com.mavic.backend.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user", schema = "restaurant")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Optional: Link to Customer, Restaurant, etc.
    @Column(name = "customerId")
    private Long customerId;

    @Column(name = "restaurantId")
    private Long restaurantId;

    // Account lockout fields
    @Column(name = "failedLoginAttempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "accountLockedUntil")
    private LocalDateTime accountLockedUntil;

    @Column(name = "lastFailedLogin")
    private LocalDateTime lastFailedLogin;

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(accountLockedUntil);
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastFailedLogin = null;
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        this.lastFailedLogin = LocalDateTime.now();
    }

    /**
     * Lock account for specified minutes
     */
    public void lockAccount(int minutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }
}
