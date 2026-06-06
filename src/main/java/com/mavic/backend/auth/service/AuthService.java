package com.mavic.backend.auth.service;

import com.mavic.backend.auth.dto.*;
import com.mavic.backend.auth.exception.AccountLockedException;
import com.mavic.backend.auth.model.User;
import com.mavic.backend.auth.repository.UserRepository;
import com.mavic.backend.common.service.EmailService;
import com.mavic.backend.customer.model.Customer;
import com.mavic.backend.customer.repository.CustomerRepository;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import com.mavic.backend.common.enums.UserRole;
import com.mavic.backend.common.security.AuditLog;
import com.mavic.backend.common.security.JwtUtil;
import com.mavic.backend.common.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;

    @Value("${admin.invite-code:ADMIN-SECRET-2026}")
    private String adminInviteCode;

    // Account lockout configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;


    /**
     * Register a new CUSTOMER with profile information
     */
    @AuditLog(action = "CUSTOMER_REGISTRATION")
    @Transactional
    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if phone already exists
        if (customerRepository.findCustomerByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already registered");
        }

        // Create customer profile first
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer = customerRepository.save(customer);

        // Create user account and link to customer
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.CUSTOMER);
        user.setCustomerId(customer.getId());
        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("New customer registered: {} (Customer ID: {})", user.getUsername(), customer.getId());

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

    /**
     * Register a new KITCHEN_STAFF member
     */
    @AuditLog(action = "KITCHEN_STAFF_REGISTRATION")
    @Transactional
    public AuthResponse registerKitchenStaff(RegisterKitchenStaffRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate restaurant exists
        if (!restaurantRepository.existsById(request.getRestaurantId())) {
            throw new RuntimeException("Restaurant not found with ID: " + request.getRestaurantId());
        }

        // Create user account
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.KITCHEN_STAFF);
        user.setRestaurantId(request.getRestaurantId());
        user.setName(request.getName());
        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("New kitchen staff registered: {} for Restaurant ID: {}", user.getUsername(), request.getRestaurantId());

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

    /**
     * Register a new DELIVERY_DRIVER
     */
    @AuditLog(action = "DELIVERY_DRIVER_REGISTRATION")
    @Transactional
    public AuthResponse registerDeliveryDriver(RegisterDeliveryDriverRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user account with driver information
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.DELIVERY_DRIVER);
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setVehicleType(request.getVehicleType());
        user.setVehicleNumber(request.getVehicleNumber());
        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("New delivery driver registered: {} (Vehicle: {} {})", 
                user.getUsername(), request.getVehicleType(), request.getVehicleNumber());

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

    /**
     * Register a new RESTAURANT_ADMIN (requires invite code)
     */
    @AuditLog(action = "ADMIN_REGISTRATION")
    @Transactional
    public AuthResponse registerAdmin(RegisterAdminRequest request) {
        // Validate invite code
        if (!adminInviteCode.equals(request.getInviteCode())) {
            log.warn("Invalid admin invite code attempt for username: {}", request.getUsername());
            throw new RuntimeException("Invalid invite code. Admin registration requires a valid invite code.");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create admin user account
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.RESTAURANT_ADMIN);
        user.setName(request.getName());
        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("New admin registered: {}", user.getUsername());

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

        // Check if account is deactivated
        if (!user.getIsActive()) {
            log.warn("Login attempt for deactivated account: {}", user.getUsername());
            throw new LockedException("Your account has been deactivated. Please contact support to reactivate your account.");
        }

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

            // If this is first login, include a flag in the response
            // Frontend should redirect to password change page
            if (user.getFirstLogin()) {
                log.info("First login detected for user: {} - password change required", user.getUsername());
            }

            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getFirstLogin()
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

    /**
     * Create admin invitation by existing admin
     * Generates temporary password and creates account with firstLogin flag
     */
    @AuditLog(action = "CREATE_ADMIN_INVITATION")
    @Transactional
    public AdminInvitationResponse createAdminInvitation(CreateAdminRequest request) {
        // Validate that current user is RESTAURANT_ADMIN
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getRole() != UserRole.RESTAURANT_ADMIN) {
            throw new RuntimeException("Only restaurant administrators can create admin accounts");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate restaurant if provided
        if (request.getRestaurantId() != null && !restaurantRepository.existsById(request.getRestaurantId())) {
            throw new RuntimeException("Restaurant not found with ID: " + request.getRestaurantId());
        }

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Create new admin user
        User newAdmin = new User();
        newAdmin.setUsername(request.getUsername());
        newAdmin.setPassword(passwordEncoder.encode(temporaryPassword));
        newAdmin.setEmail(request.getEmail());
        newAdmin.setName(request.getName());
        newAdmin.setRole(UserRole.RESTAURANT_ADMIN);
        newAdmin.setIsActive(true);
        newAdmin.setFirstLogin(true); // Flag for password change requirement
        
        if (request.getRestaurantId() != null && request.getRestaurantId() > 0) {
            newAdmin.setRestaurantId(request.getRestaurantId());
        }

        newAdmin = userRepository.save(newAdmin);

        log.info("Admin invitation created by {} for new admin: {}", currentUser.getUsername(), newAdmin.getUsername());

        // Send invitation email with credentials
        try {
            emailService.sendAdminInvitationEmail(
                    newAdmin.getEmail(),
                    newAdmin.getName(),
                    newAdmin.getUsername(),
                    temporaryPassword,
                    currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername()
            );
            log.info("Invitation email sent to: {}", newAdmin.getEmail());
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", newAdmin.getEmail(), e.getMessage());
            // Continue even if email fails - admin can still share credentials manually
        }

        // Return invitation details with temporary credentials
        return new AdminInvitationResponse(
                newAdmin.getId(),
                newAdmin.getUsername(),
                newAdmin.getEmail(),
                temporaryPassword,
                "/api/auth/login", // Login endpoint
                "Admin account created successfully. Invitation email has been sent to " + newAdmin.getEmail() + 
                ". You can also share these credentials manually if needed.",
                true
        );
    }

    /**
     * Change password for authenticated user
     */
    @AuditLog(action = "CHANGE_PASSWORD")
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Get current authenticated user
        User currentUser = securityUtils.getCurrentUser();

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Validate new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Update password and clear firstLogin flag
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser.setFirstLogin(false);
        userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getUsername());

        // Send confirmation email
        try {
            emailService.sendPasswordChangedEmail(currentUser.getEmail(), currentUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to send password change confirmation email: {}", e.getMessage());
            // Don't fail the operation if email fails
        }
    }

    /**
     * Generate secure temporary password
     * Format: 3 uppercase + 3 lowercase + 2 digits + 2 special chars (shuffled)
     */
    private String generateTemporaryPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@$!%*?&";
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Add required character types
        for (int i = 0; i < 3; i++) {
            password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        }
        for (int i = 0; i < 3; i++) {
            password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        }
        for (int i = 0; i < 2; i++) {
            password.append(digits.charAt(random.nextInt(digits.length())));
        }
        for (int i = 0; i < 2; i++) {
            password.append(special.charAt(random.nextInt(special.length())));
        }
        
        // Shuffle the password
        char[] passwordChars = password.toString().toCharArray();
        for (int i = passwordChars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordChars[i];
            passwordChars[i] = passwordChars[j];
            passwordChars[j] = temp;
        }
        
        return new String(passwordChars);
    }
}
