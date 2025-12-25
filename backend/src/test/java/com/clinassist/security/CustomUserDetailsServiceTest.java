package com.clinassist.security;

import com.clinassist.entity.User;
import com.clinassist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour CustomUserDetailsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

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
    }

    @Nested
    @DisplayName("loadUserByUsername Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load user by username")
        void loadUserByUsername_ShouldReturnUserDetails_WhenUsernameExists() {
            // Given
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

            // Then
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo("testuser");
            assertThat(userDetails.getPassword()).isEqualTo("password123");
            assertThat(userDetails.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should load user by email")
        void loadUserByUsername_ShouldReturnUserDetails_WhenEmailExists() {
            // Given
            when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com"))
                    .thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

            // Then
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void loadUserByUsername_ShouldThrow_WhenUserNotFound() {
            // Given
            when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found with username or email");
        }

        @Test
        @DisplayName("Should return inactive user with isEnabled false")
        void loadUserByUsername_ShouldReturnInactiveUser() {
            // Given
            testUser.setIsActive(false);
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

            // Then
            assertThat(userDetails.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("getAuthorities Tests")
    class GetAuthoritiesTests {

        @Test
        @DisplayName("Should return ROLE_PATIENT for patient")
        void getAuthorities_ShouldReturnPatientRole() {
            // Given
            testUser.setRole(User.Role.PATIENT);
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

            // Then
            assertThat(userDetails.getAuthorities()).hasSize(1);
            assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_PATIENT");
        }

        @Test
        @DisplayName("Should return ROLE_THERAPEUTE for therapeute")
        void getAuthorities_ShouldReturnTherapeuteRole() {
            // Given
            testUser.setRole(User.Role.THERAPEUTE);
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

            // Then
            assertThat(userDetails.getAuthorities()).hasSize(1);
            assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_THERAPEUTE");
        }

        @Test
        @DisplayName("Should return ROLE_ADMIN for admin")
        void getAuthorities_ShouldReturnAdminRole() {
            // Given
            testUser.setRole(User.Role.ADMIN);
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

            // Then
            assertThat(userDetails.getAuthorities()).hasSize(1);
            assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_ADMIN");
        }
    }
}
