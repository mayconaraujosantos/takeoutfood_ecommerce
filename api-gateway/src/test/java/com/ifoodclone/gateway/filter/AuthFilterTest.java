package com.ifoodclone.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Filter Tests")
class AuthFilterTest {

    @Mock
    private ServerWebExchange exchange;
    @Mock
    private ServerHttpRequest request;
    @Mock
    private ServerHttpResponse response;
    @Mock
    private GatewayFilterChain chain;
    @Mock
    private HttpHeaders headers;
    @Mock
    private HttpHeaders responseHeaders;
    @Mock
    private org.springframework.core.io.buffer.DataBufferFactory dataBufferFactory;
    @Mock
    private ServerHttpRequest.Builder requestBuilder;
    @Mock
    private ServerWebExchange.Builder mutator;

    private AuthFilter authFilter;
    private AuthFilter.Config config;

    // Use a proper 256-bit key for testing
    // Use a direct string secret that matches what AuthFilter expects (converted to
    // bytes)
    private static final String TEST_SECRET = "testSecretKeyForJWTTestingPurposesWithMinimum256BitsLengthForJWTHMACASHA256testSecretKey";
    private String validToken;
    private String expiredToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        config = new AuthFilter.Config();

        // Set the JWT secret using reflection
        ReflectionTestUtils.setField(authFilter, "jwtSecret", TEST_SECRET);

        // Generate test tokens
        generateTestTokens();

        // Setup essential mocks only - avoid over-mocking
        setupBasicMocks();
    }

    private void setupBasicMocks() {
        // Mock the core exchange behavior - using lenient for unnecessary stubbing
        // issues
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(request.getHeaders()).thenReturn(headers);

        // Mock path - essential for logging
        org.springframework.http.server.RequestPath mockPath = mock(org.springframework.http.server.RequestPath.class);
        lenient().when(request.getPath()).thenReturn(mockPath);
        lenient().when(mockPath.value()).thenReturn("/api/test");

        // Mock response headers - essential for adding user context
        lenient().when(response.getHeaders()).thenReturn(responseHeaders);

        // Mock request mutation chain - only when needed
        lenient().when(request.mutate()).thenReturn(requestBuilder);
        lenient().when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        lenient().when(requestBuilder.build()).thenReturn(request);

        // Mock exchange mutation chain
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        lenient().when(exchange.mutate()).thenReturn(exchangeBuilder);
        lenient().when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
        lenient().when(exchangeBuilder.build()).thenReturn(exchange);

        // Default chain behavior
        lenient().when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Mock DataBufferFactory for error handling in success tests
        lenient().when(response.bufferFactory()).thenReturn(dataBufferFactory);
        lenient().when(dataBufferFactory.wrap(any(byte[].class)))
                .thenReturn(mock(org.springframework.core.io.buffer.DataBuffer.class));
        lenient().when(response.writeWith(any())).thenReturn(Mono.empty());
    }

    private void generateTestTokens() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        // Valid token
        validToken = Jwts.builder()
                .subject("123")
                .claim("email", "test@example.com")
                .claim("roles", "CUSTOMER")
                .claim("authorities", "ROLE_CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute
                .signWith(key)
                .compact();

        // Expired token
        expiredToken = Jwts.builder()
                .subject("123")
                .claim("email", "test@example.com")
                .claim("roles", "CUSTOMER")
                .issuedAt(new Date(System.currentTimeMillis() - 120000)) // 2 minutes ago
                .expiration(new Date(System.currentTimeMillis() - 60000)) // 1 minute ago
                .signWith(key)
                .compact();

        // Invalid token
        invalidToken = "invalid.jwt.token";
    }

    @Nested
    @DisplayName("Authentication Success Tests")
    class AuthenticationSuccessTests {

        @Test
        @DisplayName("Should authenticate valid token successfully")
        void shouldAuthenticateValidTokenSuccessfully() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            // Verify successful authentication flow
            verify(chain).filter(any(ServerWebExchange.class));

            // Verify user context headers were added
            verify(requestBuilder).header("X-User-Id", "123");
            verify(requestBuilder).header("X-User-Email", "test@example.com");
            verify(requestBuilder).header("X-User-Roles", "CUSTOMER");
            verify(requestBuilder).header("X-User-Authorities", "ROLE_CUSTOMER");
            verify(requestBuilder).header("X-Authenticated", "true");
        }

        @Test
        @DisplayName("Should handle missing optional claims gracefully")
        void shouldHandleMissingOptionalClaimsGracefully() {
            // Given
            SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
            String minimalToken = Jwts.builder()
                    .subject("456")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(key)
                    .compact();

            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + minimalToken);

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(chain).filter(any(ServerWebExchange.class));
            verify(requestBuilder).header("X-User-Id", "456");
            verify(requestBuilder).header("X-User-Email", "");
            verify(requestBuilder).header("X-User-Roles", "");
            verify(requestBuilder).header("X-User-Authorities", "");
            verify(requestBuilder).header("X-Authenticated", "true");
        }

        @Test
        @DisplayName("Should extract all user context information")
        void shouldExtractAllUserContextInformation() {
            // Given
            SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
            String fullToken = Jwts.builder()
                    .subject("789")
                    .claim("email", "admin@example.com")
                    .claim("roles", "ADMIN")
                    .claim("authorities", "ROLE_ADMIN,ROLE_USER")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(key)
                    .compact();

            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + fullToken);

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(requestBuilder).header("X-User-Id", "789");
            verify(requestBuilder).header("X-User-Email", "admin@example.com");
            verify(requestBuilder).header("X-User-Roles", "ADMIN");
            verify(requestBuilder).header("X-User-Authorities", "ROLE_ADMIN,ROLE_USER");
            verify(requestBuilder).header("X-Authenticated", "true");
        }
    }

    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {

        @Test
        @DisplayName("Should reject request without authorization header")
        void shouldRejectRequestWithoutAuthorizationHeader() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject request with null authorization header")
        void shouldRejectRequestWithNullAuthorizationHeader() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject request without Bearer prefix")
        void shouldRejectRequestWithoutBearerPrefix() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + validToken);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + expiredToken);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject invalid token")
        void shouldRejectInvalidToken() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + invalidToken);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject token with wrong signature")
        void shouldRejectTokenWithWrongSignature() {
            // Given
            SecretKey wrongKey = Keys.hmacShaKeyFor(
                    "wrongsecret123wrongsecret123wrongsecret123wrongsecret123".getBytes(StandardCharsets.UTF_8));
            String wrongSignatureToken = Jwts.builder()
                    .subject("123")
                    .claim("email", "test@example.com")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(wrongKey)
                    .compact();

            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + wrongSignatureToken);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject malformed JWT token")
        void shouldRejectMalformedJwtToken() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer malformed.jwt");
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token expiration correctly")
        void shouldValidateTokenExpirationCorrectly() {
            // Given
            SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

            // Token that expires in 5 seconds
            String soonToExpireToken = Jwts.builder()
                    .subject("123")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 5000))
                    .signWith(key)
                    .compact();

            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + soonToExpireToken);

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then - Should still be valid
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should validate token without expiration claim")
        void shouldValidateTokenWithoutExpirationClaim() {
            // Given
            SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

            String noExpirationToken = Jwts.builder()
                    .subject("123")
                    .issuedAt(new Date())
                    // No expiration set
                    .signWith(key)
                    .compact();

            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + noExpirationToken);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then - Should be rejected (missing expiration)
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }
    }

    @Nested
    @DisplayName("Error Response Tests")
    class ErrorResponseTests {

        @Test
        @DisplayName("Should set correct error response for missing authorization")
        void shouldSetCorrectErrorResponseForMissingAuthorization() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(response.getHeaders()).add("Content-Type", "application/json");
        }

        @Test
        @DisplayName("Should set correct error response for invalid token")
        void shouldSetCorrectErrorResponseForInvalidToken() {
            // Given
            when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid");
            setupErrorResponse();

            GatewayFilter filter = authFilter.apply(config);

            // When
            Mono<Void> result = filter.filter(exchange, chain);

            // Then
            assertThat(result).isNotNull();
            result.block(); // Execute the filter

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(response.getHeaders()).add("Content-Type", "application/json");
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should create config with default values")
        void shouldCreateConfigWithDefaultValues() {
            // Given & When
            AuthFilter.Config testConfig = new AuthFilter.Config();

            // Then
            assertThat(testConfig.isRequireAuth()).isTrue();
            assertThat(testConfig.getBypassPaths()).isEmpty();
        }

        @Test
        @DisplayName("Should allow setting config values")
        void shouldAllowSettingConfigValues() {
            // Given
            AuthFilter.Config testConfig = new AuthFilter.Config();

            // When
            testConfig.setRequireAuth(false);
            testConfig.setBypassPaths("/health,/info");

            // Then
            assertThat(testConfig.isRequireAuth()).isFalse();
            assertThat(testConfig.getBypassPaths()).isEqualTo("/health,/info");
        }
    }

    @SuppressWarnings("unchecked")
    private void setupErrorResponse() {
        when(response.getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.bufferFactory().wrap(any(byte[].class)))
                .thenReturn(mock(org.springframework.core.io.buffer.DataBuffer.class));
        when(response.writeWith(any(Mono.class))).thenReturn(Mono.empty());
    }
}