package com.clinassist.controller;

import com.clinassist.dto.PatientCreateRequest;
import com.clinassist.dto.PatientDTO;
import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.entity.User;
import com.clinassist.security.JwtTokenProvider;
import com.clinassist.service.AuthService;
import com.clinassist.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour AuthController
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private AuthResponse authResponse;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .user(AuthResponse.UserInfo.builder()
                        .id(1L)
                        .username("testuser")
                        .email("test@example.com")
                        .firstName("Test")
                        .lastName("User")
                        .role(User.Role.PATIENT)
                        .build())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setRole(User.Role.PATIENT);

        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("POST /auth/register - Should register user successfully")
    @WithMockUser
    void register_ShouldReturnCreated() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /auth/login - Should authenticate user")
    @WithMockUser
    void login_ShouldReturnOk() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"));
    }

    @Test
    @DisplayName("POST /auth/refresh - Should refresh token")
    @WithMockUser
    void refreshToken_ShouldReturnNewTokens() throws Exception {
        when(authService.refreshToken(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/refresh")
                .with(csrf())
                .header("X-Refresh-Token", "old-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"));
    }

    @Test
    @DisplayName("POST /auth/register/patient - Should register patient")
    @WithMockUser
    void registerPatient_ShouldReturnCreated() throws Exception {
        PatientCreateRequest patientRequest = new PatientCreateRequest();
        patientRequest.setEmail("patient@example.com");
        patientRequest.setFirstName("Patient");
        patientRequest.setLastName("Test");

        PatientDTO patientDTO = PatientDTO.builder()
                .id(1L)
                .patientCode("PAT-001")
                .email("patient@example.com")
                .build();

        when(patientService.createPatient(any(PatientCreateRequest.class))).thenReturn(patientDTO);

        mockMvc.perform(post("/auth/register/patient")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientCode").value("PAT-001"));
    }
}
