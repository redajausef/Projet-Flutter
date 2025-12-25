package com.clinassist.service;

import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.entity.User;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.TherapeuteRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TherapeuteRepository therapeuteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

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
        testUser.setPassword("password123");
        testUser.setFirstName("Dr Sophie");
        testUser.setLastName("Martin");
        testUser.setRole(User.Role.THERAPEUTE);
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("dr.martin");
        loginRequest.setPassword("test123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail("dr.martin", "dr.martin"))
            .thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("access_token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh_token");
        when(tokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void register_WithValidData_ShouldReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole(User.Role.PATIENT);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh_token");
        when(tokenProvider.getExpirationTime()).thenReturn(3600L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        when(tokenProvider.validateToken("valid_refresh_token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("valid_refresh_token")).thenReturn("dr.martin");
        when(userRepository.findByUsername("dr.martin")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(anyString())).thenReturn("new_access_token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("new_refresh_token");
        when(tokenProvider.getExpirationTime()).thenReturn(3600L);

        AuthResponse response = authService.refreshToken("valid_refresh_token");

        assertNotNull(response);
    }
}
