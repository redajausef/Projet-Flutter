package com.clinassist.service;

import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.entity.User;
import com.clinassist.repository.UserRepository;
import com.clinassist.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("dr.martin");
        testUser.setEmail("dr.martin@test.com");
        testUser.setPassword("encoded_password");
        testUser.setFirstName("Dr Sophie");
        testUser.setLastName("Martin");
        testUser.setRole(User.UserRole.THERAPEUTE);
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("dr.martin");
        loginRequest.setPassword("test123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt_token");
        when(userRepository.findByUsername("dr.martin")).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should check if username exists")
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsByUsername("dr.martin")).thenReturn(true);

        boolean exists = authService.existsByUsername("dr.martin");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        when(userRepository.existsByEmail("dr.martin@test.com")).thenReturn(true);

        boolean exists = authService.existsByEmail("dr.martin@test.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should encode password during registration")
    void register_ShouldEncodePassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.register(request);

        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername_ShouldReturnUser_WhenExists() {
        when(userRepository.findByUsername("dr.martin")).thenReturn(Optional.of(testUser));

        Optional<User> result = authService.findByUsername("dr.martin");

        assertTrue(result.isPresent());
        assertEquals("dr.martin", result.get().getUsername());
    }
}
