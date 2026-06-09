package com.mavic.backend.service;

import com.mavic.backend.auth.dto.AuthResponse;
import com.mavic.backend.auth.dto.AdminInvitationResponse;
import com.mavic.backend.auth.dto.CreateAdminRequest;
import com.mavic.backend.auth.dto.LoginRequest;
import com.mavic.backend.auth.dto.RegisterRequest;
import com.mavic.backend.auth.model.User;
import com.mavic.backend.auth.repository.UserRepository;
import com.mavic.backend.auth.service.AuthService;
import com.mavic.backend.common.enums.UserRole;
import com.mavic.backend.common.security.SecurityUtils;
import com.mavic.backend.common.security.JwtUtil;
import com.mavic.backend.common.service.EmailService;
import com.mavic.backend.auth.exception.AccountLockedException;
import com.mavic.backend.customer.repository.CustomerRepository;
import com.mavic.backend.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setRole(UserRole.CUSTOMER);
        user.setIsActive(true);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setRole("CUSTOMER");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken("testuser", "CUSTOMER")).thenReturn("jwtToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwtToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser", "CUSTOMER")).thenReturn("jwtToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_LockedAccount_ThrowsLockedException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        user.lockAccount(15);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(LockedException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_BadCredentials_IncrementsAttempts() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(AccountLockedException.class, () -> authService.login(request));
        verify(userRepository).saveAndFlush(user);
        assertEquals(1, user.getFailedLoginAttempts());
    }

    @Test
    void createAdminInvitation_EmailSent_ReturnsSuccessMessage() {
        CreateAdminRequest request = new CreateAdminRequest();
        request.setUsername("newadmin");
        request.setEmail("newadmin@example.com");
        request.setName("New Admin");
        request.setRestaurantId(5L);

        User currentAdmin = new User();
        currentAdmin.setUsername("owner");
        currentAdmin.setName("Owner Admin");
        currentAdmin.setRole(UserRole.RESTAURANT_ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(currentAdmin);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(restaurantRepository.existsById(5L)).thenReturn(true);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(99L);
            return savedUser;
        });
        when(emailService.sendAdminInvitationEmail(
                eq("newadmin@example.com"),
                eq("New Admin"),
                eq("newadmin"),
                any(String.class),
                eq("Owner Admin")
        )).thenReturn(true);

        AdminInvitationResponse response = authService.createAdminInvitation(request);

        assertEquals(99L, response.getUserId());
        assertEquals("newadmin@example.com", response.getEmail());
        assertTrue(response.isRequirePasswordChange());
        assertTrue(response.getMessage().contains("Invitation email has been sent"));
        verify(emailService).sendAdminInvitationEmail(
                eq("newadmin@example.com"),
                eq("New Admin"),
                eq("newadmin"),
                any(String.class),
                eq("Owner Admin")
        );
    }

    @Test
    void createAdminInvitation_EmailFails_ReturnsManualShareMessage() {
        CreateAdminRequest request = new CreateAdminRequest();
        request.setUsername("backupadmin");
        request.setEmail("backupadmin@example.com");
        request.setName("Backup Admin");

        User currentAdmin = new User();
        currentAdmin.setUsername("owner");
        currentAdmin.setRole(UserRole.RESTAURANT_ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(currentAdmin);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(100L);
            return savedUser;
        });
        when(emailService.sendAdminInvitationEmail(
                eq("backupadmin@example.com"),
                eq("Backup Admin"),
                eq("backupadmin"),
                any(String.class),
                eq("owner")
        )).thenReturn(false);

        AdminInvitationResponse response = authService.createAdminInvitation(request);

        assertEquals(100L, response.getUserId());
        assertTrue(response.getMessage().contains("could not be sent"));
        assertTrue(response.getMessage().contains("share the temporary credentials manually"));
        verify(restaurantRepository, never()).existsById(anyLong());
    }
}
