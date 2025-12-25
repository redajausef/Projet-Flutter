package com.clinassist.controller;

import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.entity.User;
import com.clinassist.service.AuthService;
import com.clinassist.service.PatientService;
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

    @Mock
    private PatientService patientService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
            .id(1L)
            .username("dr.martin")
            .email("dr.martin@test.com")
            .firstName("Dr Sophie")
            .lastName("Martin")
            .role(User.Role.THERAPEUTE)
            .build();

        testAuthResponse = AuthResponse.builder()
            .accessToken("access_token")
            .refreshToken("refresh_token")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .user(userInfo)
            .build();
    }

    @Test
    @DisplayName("POST /auth/login should return token for valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("dr.martin");
        loginRequest.setPassword("test123");

        when(authService.login(any(LoginRequest.class))).thenReturn(testAuthResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access_token", response.getBody().getAccessToken());
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
        request.setRole(User.Role.PATIENT);

        when(authService.register(any(RegisterRequest.class))).thenReturn(testAuthResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("POST /auth/refresh should return new tokens")
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        when(authService.refreshToken("valid_refresh_token")).thenReturn(testAuthResponse);

        ResponseEntity<AuthResponse> response = authController.refreshToken("valid_refresh_token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Login should call auth service")
    void login_ShouldCallAuthService() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("dr.martin");
        loginRequest.setPassword("test123");

        when(authService.login(any(LoginRequest.class))).thenReturn(testAuthResponse);

        authController.login(loginRequest);

        verify(authService, times(1)).login(any(LoginRequest.class));
    }
}
