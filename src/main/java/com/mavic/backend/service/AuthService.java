package com.mavic.backend.service;

import com.mavic.backend.dto.AuthResponse;
import com.mavic.backend.dto.LoginRequest;
import com.mavic.backend.dto.RegisterRequest;
import com.mavic.backend.exception.AccountLockedException;
import com.mavic.backend.model.User;
import com.mavic.backend.model.enums.UserRole;
import com.mavic.backend.repository.UserRepository;
import com.mavic.backend.security.AuditLog;
import com.mavic.backend.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Account lockout configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @AuditLog(action = "USER_REGISTRATION")
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setIsActive(true);
        
        // Only set customerId if it's provided and not null/zero
        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            user.setCustomerId(request.getCustomerId());
        }
        
        // Only set restaurantId if it's provided and not null/zero
        if (request.getRestaurantId() != null && request.getRestaurantId() > 0) {
            user.setRestaurantId(request.getRestaurantId());
        }

        user = userRepository.save(user);

        log.info("New user registered: {}", user.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    @AuditLog(action = "USER_LOGIN")
    public AuthResponse login(LoginRequest request) {
        // Get user first to check account status
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Login attempt for locked account: {}", user.getUsername());
            throw new LockedException("Account is locked due to multiple failed login attempts. Please try again later.");
        }

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                resetFailedAttemptsInNewTransaction(user.getId());
            }

            log.info("Successful login for user: {}", user.getUsername());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );

        } catch (BadCredentialsException e) {
            // Handle failed login in a separate transaction
            handleFailedLoginInNewTransaction(user.getId());
            // This will never be reached as handleFailedLoginInNewTransaction throws exception
            return null;
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void resetFailedAttemptsInNewTransaction(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.resetFailedAttempts();
        userRepository.saveAndFlush(user);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void handleFailedLoginInNewTransaction(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        user.incrementFailedAttempts();
        int attemptsRemaining = MAX_FAILED_ATTEMPTS - user.getFailedLoginAttempts();
        
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount(LOCKOUT_DURATION_MINUTES);
            log.warn("Account locked for user: {} after {} failed attempts", 
                    user.getUsername(), MAX_FAILED_ATTEMPTS);
            userRepository.saveAndFlush(user);
            
            throw new AccountLockedException(
                    String.format("Account has been locked due to %d failed login attempts. Please try again after %d minutes.", 
                            MAX_FAILED_ATTEMPTS, LOCKOUT_DURATION_MINUTES),
                    0,
                    true
            );
        } else {
            log.warn("Failed login attempt {} of {} for user: {}", 
                    user.getFailedLoginAttempts(), MAX_FAILED_ATTEMPTS, user.getUsername());
            userRepository.saveAndFlush(user);
            
            throw new AccountLockedException(
                    String.format("Invalid username or password. Warning: %d attempt%s remaining before account lockout.", 
                            attemptsRemaining, attemptsRemaining == 1 ? "" : "s"),
                    attemptsRemaining,
                    false
            );
        }
    }
}
