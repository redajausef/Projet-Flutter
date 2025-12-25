package com.clinassist.controller;

import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        testAuthResponse = AuthResponse.builder()
            .token("jwt_token")
            .type("Bearer")
            .userId(1L)
            .username("dr.martin")
            .email("dr.martin@test.com")
            .role("THERAPEUTE")
            .build();
    }

    @Test
    @DisplayName("POST /auth/login should return token for valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("dr.martin");
        loginRequest.setPassword("test123");

        when(authService.login(any(LoginRequest.class))).thenReturn(testAuthResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt_token", response.getBody().getToken());
    }

    @Test
    @DisplayName("POST /auth/register should register new user")
    void register_WithValidData_ShouldReturnCreated() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail(anyString())).thenReturn(false);
        when(authService.register(any(RegisterRequest.class))).thenReturn(testAuthResponse);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /auth/register should return error for existing username")
    void register_WithExistingUsername_ShouldReturnBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");

        when(authService.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /auth/register should return error for existing email")
    void register_WithExistingEmail_ShouldReturnBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@test.com");

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail("existing@test.com")).thenReturn(true);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
