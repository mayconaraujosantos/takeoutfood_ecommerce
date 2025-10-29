package com.ifoodclone.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ifoodclone.auth.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private User user;

    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckpXVFRlc3RpbmdQdXJwb3Nlc1dpdGhNaW5pbXVtTGVuZ3RoUmVxdWlyZWQ=";
    private static final long TEST_EXPIRATION = 60000; // 1 minute
    private static final long TEST_REFRESH_EXPIRATION = 300000; // 5 minutes

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Set test values using reflection
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", TEST_REFRESH_EXPIRATION);

        // Create test user
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .role(User.UserRole.CUSTOMER)
                .emailVerified(true)
                .active(true)
                .build();

        userDetails = user;
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid access token")
        void shouldGenerateValidAccessToken() {
            // When
            String token = jwtService.generateToken(userDetails);

            // Then
            assertThat(token).isNotNull().isNotEmpty();
            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
            assertThat(jwtService.isAccessToken(token)).isTrue();
            assertThat(jwtService.isRefreshToken(token)).isFalse();
        }

        @Test
        @DisplayName("Should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            // When
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Then
            assertThat(refreshToken).isNotNull().isNotEmpty();
            assertThat(jwtService.isTokenValid(refreshToken, userDetails)).isTrue();
            assertThat(jwtService.isRefreshToken(refreshToken)).isTrue();
            assertThat(jwtService.isAccessToken(refreshToken)).isFalse();
        }

        @Test
        @DisplayName("Should generate token with custom claims")
        void shouldGenerateTokenWithCustomClaims() {
            // Given
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("customClaim", "customValue");
            extraClaims.put("permissions", "read,write");

            // When
            String token = jwtService.generateToken(extraClaims, userDetails);

            // Then
            assertThat(token).isNotNull().isNotEmpty();
            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();

            // Verify custom claims are included
            String customClaim = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));
            assertThat(customClaim).isEqualTo("customValue");

            String permissions = jwtService.extractClaim(token, claims -> claims.get("permissions", String.class));
            assertThat(permissions).isEqualTo("read,write");
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            // Given
            User anotherUser = User.builder()
                    .id(2L)
                    .email("another@example.com")
                    .firstName("Another")
                    .lastName("User")
                    .role(User.UserRole.ADMIN)
                    .active(true)
                    .emailVerified(true)
                    .build();

            // When
            String token1 = jwtService.generateToken(userDetails);
            String token2 = jwtService.generateToken(anotherUser);

            // Then
            assertThat(token1).isNotEqualTo(token2);
            assertThat(jwtService.extractUsername(token1)).isEqualTo("test@example.com");
            assertThat(jwtService.extractUsername(token2)).isEqualTo("another@example.com");
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        private String validToken;

        @BeforeEach
        void setUp() {
            validToken = jwtService.generateToken(userDetails);
        }

        @Test
        @DisplayName("Should extract username correctly")
        void shouldExtractUsername() {
            // When
            String username = jwtService.extractUsername(validToken);

            // Then
            assertThat(username).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should extract user ID correctly")
        void shouldExtractUserId() {
            // When
            Long userId = jwtService.extractUserId(validToken);

            // Then
            assertThat(userId).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should extract role correctly")
        void shouldExtractRole() {
            // When
            String role = jwtService.extractRole(validToken);

            // Then
            assertThat(role).isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("Should extract expiration date")
        void shouldExtractExpirationDate() {
            // When
            var expiration = jwtService.extractExpiration(validToken);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration.getTime()).isGreaterThan(System.currentTimeMillis());
        }

        @Test
        @DisplayName("Should extract token type")
        void shouldExtractTokenType() {
            // Given
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // When & Then
            assertThat(jwtService.extractTokenType(accessToken)).isEqualTo("ACCESS");
            assertThat(jwtService.extractTokenType(refreshToken)).isEqualTo("REFRESH");
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void shouldValidateCorrectToken() {
            // Given
            String token = jwtService.generateToken(userDetails);

            // When & Then
            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Should reject token for wrong user")
        void shouldRejectTokenForWrongUser() {
            // Given
            String token = jwtService.generateToken(userDetails);

            User wrongUser = new User();
            wrongUser.setEmail("wrong@example.com");

            // When & Then
            assertThat(jwtService.isTokenValid(token, wrongUser)).isFalse();
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            // Given
            String malformedToken = "invalid.token.here";

            // When & Then
            assertThatThrownBy(() -> jwtService.isTokenValid(malformedToken, userDetails))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("Should reject token with wrong signature")
        void shouldRejectTokenWithWrongSignature() {
            // Given
            String token = jwtService.generateToken(userDetails);
            String tokenWithWrongSignature = token.substring(0, token.length() - 10) + "wrongsign";

            // When & Then
            assertThatThrownBy(() -> jwtService.isTokenValid(tokenWithWrongSignature, userDetails))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("Should validate token type correctly")
        void shouldValidateTokenTypeCorrectly() {
            // Given
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // When & Then
            assertThat(jwtService.validateTokenType(accessToken, "ACCESS")).isTrue();
            assertThat(jwtService.validateTokenType(refreshToken, "REFRESH")).isTrue();
            assertThat(jwtService.validateTokenType(accessToken, "REFRESH")).isFalse();
            assertThat(jwtService.validateTokenType(refreshToken, "ACCESS")).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Expiration Tests")
    class TokenExpirationTests {

        @Test
        @DisplayName("Should detect non-expired token")
        void shouldDetectNonExpiredToken() {
            // Given
            String token = jwtService.generateToken(userDetails);

            // When & Then
            assertThat(jwtService.isTokenExpired(token)).isFalse();
        }

        @Test
        @DisplayName("Should calculate remaining time correctly")
        void shouldCalculateRemainingTimeCorrectly() {
            // Given
            String token = jwtService.generateToken(userDetails);

            // When
            long remainingTime = jwtService.getTokenRemainingTime(token);

            // Then
            assertThat(remainingTime)
                    .isGreaterThan(0)
                    .isLessThanOrEqualTo(TEST_EXPIRATION / 1000);
        }

        @Test
        @DisplayName("Should detect token expiring soon")
        void shouldDetectTokenExpiringSoon() {
            // Given
            String token = jwtService.generateToken(userDetails);

            // When & Then
            assertThat(jwtService.isTokenExpiringSoon(token, 120)).isTrue(); // 2 minutes threshold
            assertThat(jwtService.isTokenExpiringSoon(token, 30)).isFalse(); // 30 seconds threshold
        }

        @Test
        @DisplayName("Should handle expired token gracefully")
        void shouldHandleExpiredTokenGracefully() {
            // Given - create a pre-expired token manually using JWT builder
            String expiredToken = Jwts.builder()
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis() - 2000)) // 2 seconds ago
                    .expiration(new Date(System.currentTimeMillis() - 1000)) // 1 second ago (expired)
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET)))
                    .compact();

            // When & Then - all operations on expired token should throw
            // ExpiredJwtException
            assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class)
                    .hasMessageContaining("JWT expired");

            assertThatThrownBy(() -> jwtService.isTokenExpired(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class);

            // The single-parameter isTokenValid method catches exceptions and returns false
            assertThat(jwtService.isTokenValid(expiredToken)).isFalse();

            // The two-parameter version doesn't catch exceptions
            assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                    .isInstanceOf(ExpiredJwtException.class);
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return correct expiration times")
        void shouldReturnCorrectExpirationTimes() {
            // When & Then
            assertThat(jwtService.getExpirationTime()).isEqualTo(TEST_EXPIRATION);
            assertThat(jwtService.getRefreshExpirationTime()).isEqualTo(TEST_REFRESH_EXPIRATION);
        }

        @Test
        @DisplayName("Should generate long-lived token for development")
        void shouldGenerateLongLivedTokenForDevelopment() {
            // Given
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "dev@example.com");
            claims.put("role", "DEVELOPER");

            // When
            String longLivedToken = jwtService.generateLongLivedToken(claims, 30);

            // Then
            assertThat(longLivedToken).isNotNull().isNotEmpty();

            long remainingTime = jwtService.getTokenRemainingTime(longLivedToken);
            long expectedMinTime = 29 * 24 * 60 * 60; // 29 days in seconds

            assertThat(remainingTime).isGreaterThan(expectedMinTime);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null token gracefully")
        void shouldHandleNullTokenGracefully() {
            // When & Then
            assertThatThrownBy(() -> jwtService.extractUsername(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle empty token gracefully")
        void shouldHandleEmptyTokenGracefully() {
            // When & Then
            assertThatThrownBy(() -> jwtService.extractUsername(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle invalid token format")
        void shouldHandleInvalidTokenFormat() {
            // Given
            String invalidToken = "not.a.jwt.token";

            // When & Then
            assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                    .isInstanceOf(MalformedJwtException.class);
        }
    }
}