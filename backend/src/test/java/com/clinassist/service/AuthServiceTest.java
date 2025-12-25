package com.clinassist.service;

import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Therapeute;
import com.clinassist.entity.User;
import com.clinassist.exception.BadRequestException;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.TherapeuteRepository;
import com.clinassist.repository.UserRepository;
import com.clinassist.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService
 */
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
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.PATIENT)
                .isActive(true)
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

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register patient successfully")
        void register_ShouldCreatePatient_WhenRoleIsPatient() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(patientRepository.save(any(Patient.class))).thenReturn(new Patient());
            when(tokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600000L);

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            verify(patientRepository).save(any(Patient.class));
            verify(therapeuteRepository, never()).save(any(Therapeute.class));
        }

        @Test
        @DisplayName("Should register therapeute successfully")
        void register_ShouldCreateTherapeute_WhenRoleIsTherapeute() {
            // Given
            registerRequest.setRole(User.Role.THERAPEUTE);
            testUser.setRole(User.Role.THERAPEUTE);

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(therapeuteRepository.save(any(Therapeute.class))).thenReturn(new Therapeute());
            when(tokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600000L);

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            verify(therapeuteRepository).save(any(Therapeute.class));
            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw exception when username exists")
        void register_ShouldThrow_WhenUsernameExists() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Username is already taken");
        }

        @Test
        @DisplayName("Should throw exception when email exists")
        void register_ShouldThrow_WhenEmailExists() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email is already registered");
        }

        @Test
        @DisplayName("Should register admin without creating patient or therapeute")
        void register_ShouldNotCreateEntity_WhenRoleIsAdmin() {
            // Given
            registerRequest.setRole(User.Role.ADMIN);
            testUser.setRole(User.Role.ADMIN);

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(tokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600000L);

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            verify(patientRepository, never()).save(any(Patient.class));
            verify(therapeuteRepository, never()).save(any(Therapeute.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void login_ShouldReturnAuthResponse_WhenCredentialsValid() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(tokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600000L);

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void login_ShouldThrow_WhenUserNotFound() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("RefreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_ShouldReturnNewTokens_WhenTokenValid() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(tokenProvider.generateAccessToken("testuser")).thenReturn("new-access-token");
            when(tokenProvider.generateRefreshToken("testuser")).thenReturn("new-refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600000L);

            // When
            AuthResponse response = authService.refreshToken(refreshToken);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("Should throw exception when refresh token invalid")
        void refreshToken_ShouldThrow_WhenTokenInvalid() {
            // Given
            String invalidToken = "invalid-token";
            when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("Should throw exception when user not found for token")
        void refreshToken_ShouldThrow_WhenUserNotFound() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("nonexistent");
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
