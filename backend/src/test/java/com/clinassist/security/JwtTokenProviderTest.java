package com.clinassist.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires pour JwtTokenProvider
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Base64 encoded secret key (256 bits minimum for HS256)
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1hbmQtdmFsaWRhdGlvbi10ZXN0aW5n";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour
    private static final Long TEST_REFRESH_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpiration", TEST_REFRESH_EXPIRATION);
    }

    @Nested
    @DisplayName("generateAccessToken Tests")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Should generate access token from username")
        void generateAccessToken_ShouldGenerateToken_FromUsername() {
            // Given
            String username = "testuser";

            // When
            String token = jwtTokenProvider.generateAccessToken(username);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate access token from authentication")
        void generateAccessToken_ShouldGenerateToken_FromAuthentication() {
            // Given
            UserDetails userDetails = new User(
                    "testuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // When
            String token = jwtTokenProvider.generateAccessToken(authentication);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken Tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate refresh token")
        void generateRefreshToken_ShouldGenerateToken() {
            // Given
            String username = "testuser";

            // When
            String token = jwtTokenProvider.generateRefreshToken(username);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void generateRefreshToken_ShouldGenerateDifferentTokens() {
            // Given
            String user1 = "user1";
            String user2 = "user2";

            // When
            String token1 = jwtTokenProvider.generateRefreshToken(user1);
            String token2 = jwtTokenProvider.generateRefreshToken(user2);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken Tests")
    class GetUsernameFromTokenTests {

        @Test
        @DisplayName("Should extract username from valid token")
        void getUsernameFromToken_ShouldReturnUsername() {
            // Given
            String username = "testuser";
            String token = jwtTokenProvider.generateAccessToken(username);

            // When
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

            // Then
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("Should extract username from refresh token")
        void getUsernameFromToken_ShouldWorkWithRefreshToken() {
            // Given
            String username = "refreshuser";
            String token = jwtTokenProvider.generateRefreshToken(username);

            // When
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

            // Then
            assertThat(extractedUsername).isEqualTo(username);
        }
    }

    @Nested
    @DisplayName("validateToken Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for valid token")
        void validateToken_ShouldReturnTrue_ForValidToken() {
            // Given
            String token = jwtTokenProvider.generateAccessToken("testuser");

            // When
            boolean isValid = jwtTokenProvider.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void validateToken_ShouldThrow_ForMalformedToken() {
            // Given
            String malformedToken = "invalid.token.here";

            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(malformedToken))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void validateToken_ShouldThrow_ForEmptyToken() {
            // Given
            String emptyToken = "";

            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(emptyToken))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Should throw exception for token with wrong signature")
        void validateToken_ShouldThrow_ForWrongSignature() {
            // Given - create token, then modify it
            String token = jwtTokenProvider.generateAccessToken("testuser");
            String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(tamperedToken))
                    .isInstanceOf(JwtException.class);
        }
    }

    @Nested
    @DisplayName("getExpirationTime Tests")
    class GetExpirationTimeTests {

        @Test
        @DisplayName("Should return configured expiration time")
        void getExpirationTime_ShouldReturnConfiguredValue() {
            // When
            Long expirationTime = jwtTokenProvider.getExpirationTime();

            // Then
            assertThat(expirationTime).isEqualTo(TEST_EXPIRATION);
        }
    }

    @Nested
    @DisplayName("Token Consistency Tests")
    class TokenConsistencyTests {

        @Test
        @DisplayName("Should maintain data integrity through token lifecycle")
        void tokenLifecycle_ShouldMaintainDataIntegrity() {
            // Given
            String originalUsername = "consistency-test-user";

            // When
            String accessToken = jwtTokenProvider.generateAccessToken(originalUsername);
            String extractedFromAccess = jwtTokenProvider.getUsernameFromToken(accessToken);

            String refreshToken = jwtTokenProvider.generateRefreshToken(originalUsername);
            String extractedFromRefresh = jwtTokenProvider.getUsernameFromToken(refreshToken);

            // Then
            assertThat(extractedFromAccess).isEqualTo(originalUsername);
            assertThat(extractedFromRefresh).isEqualTo(originalUsername);
            assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
            assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        }

        @Test
        @DisplayName("Access and refresh tokens should be different")
        void tokens_ShouldBeDifferent() {
            // Given
            String username = "testuser";

            // When
            String accessToken = jwtTokenProvider.generateAccessToken(username);
            String refreshToken = jwtTokenProvider.generateRefreshToken(username);

            // Then
            assertThat(accessToken).isNotEqualTo(refreshToken);
        }
    }
}
