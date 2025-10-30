package com.ifoodclone.user.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import com.ifoodclone.user.config.UserSecurityConfig.UserContext;
import com.ifoodclone.user.config.UserSecurityConfig.UserContextFilter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Security Config Tests")
class UserSecurityConfigTest {

    @Mock
    private FilterChain filterChain;

    private UserContextFilter userContextFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        userContextFilter = new UserContextFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Clear any existing context
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("UserContextFilter Tests")
    class UserContextFilterTests {

        @Test
        @DisplayName("Should skip authentication for excluded paths")
        void shouldSkipAuthenticationForExcludedPaths() throws ServletException, IOException {
            // Given
            request.setRequestURI("/actuator/health");

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(UserContext.getUserId()).isNull();
        }

        @Test
        @DisplayName("Should skip authentication for actuator info endpoint")
        void shouldSkipAuthenticationForActuatorInfoEndpoint() throws ServletException, IOException {
            // Given
            request.setRequestURI("/actuator/info");

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should skip authentication for API docs")
        void shouldSkipAuthenticationForApiDocs() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api-docs/swagger-ui.html");

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should authenticate valid request with headers")
        void shouldAuthenticateValidRequestWithHeaders() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Id", "123");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            request.addHeader("X-Authenticated", "true");

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(UserContext.getUserId()).isEqualTo(123L);
            assertThat(UserContext.getUserEmail()).isEqualTo("test@example.com");
            assertThat(UserContext.getUserRoles()).isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("Should reject request without authenticated header")
        void shouldRejectRequestWithoutAuthenticatedHeader() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Id", "123");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            // Missing X-Authenticated header

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getContentAsString()).contains("Authentication required");
        }

        @Test
        @DisplayName("Should reject request with false authenticated header")
        void shouldRejectRequestWithFalseAuthenticatedHeader() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Id", "123");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            request.addHeader("X-Authenticated", "false");

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(401);
        }

        @Test
        @DisplayName("Should reject request without user ID")
        void shouldRejectRequestWithoutUserId() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            request.addHeader("X-Authenticated", "true");
            // Missing X-User-Id header

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(401);
        }

        @Test
        @DisplayName("Should handle invalid user ID gracefully")
        void shouldHandleInvalidUserIdGracefully() {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Id", "invalid-id");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            request.addHeader("X-Authenticated", "true");

            // When & Then
            assertThatThrownBy(() -> userContextFilter.doFilterInternal(request, response, filterChain))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("Should clear context after request processing")
        void shouldClearContextAfterRequestProcessing() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Id", "123");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            request.addHeader("X-Authenticated", "true");

            doAnswer(invocation -> {
                // Verify context is set during filter execution
                assertThat(UserContext.getUserId()).isEqualTo(123L);
                assertThat(UserContext.getUserEmail()).isEqualTo("test@example.com");
                assertThat(UserContext.getUserRoles()).isEqualTo("CUSTOMER");
                return null;
            }).when(filterChain).doFilter(request, response);

            // When
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Then - Context should be cleared after filter
            assertThat(UserContext.getUserId()).isNull();
            assertThat(UserContext.getUserEmail()).isNull();
            assertThat(UserContext.getUserRoles()).isNull();
        }

        @Test
        @DisplayName("Should clear context even when exception occurs")
        void shouldClearContextEvenWhenExceptionOccurs() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/users/profile");
            request.addHeader("X-User-Id", "123");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Roles", "CUSTOMER");
            request.addHeader("X-Authenticated", "true");

            doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

            // When & Then
            assertThatThrownBy(() -> userContextFilter.doFilterInternal(request, response, filterChain))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");

            // Context should still be cleared
            assertThat(UserContext.getUserId()).isNull();
            assertThat(UserContext.getUserEmail()).isNull();
            assertThat(UserContext.getUserRoles()).isNull();
        }
    }

    @Nested
    @DisplayName("UserContext Tests")
    class UserContextTests {

        @Test
        @DisplayName("Should store and retrieve user context")
        void shouldStoreAndRetrieveUserContext() {
            // When
            UserContext.setUserId(456L);
            UserContext.setUserEmail("admin@example.com");
            UserContext.setUserRoles("ADMIN,CUSTOMER");

            // Then
            assertThat(UserContext.getUserId()).isEqualTo(456L);
            assertThat(UserContext.getUserEmail()).isEqualTo("admin@example.com");
            assertThat(UserContext.getUserRoles()).isEqualTo("ADMIN,CUSTOMER");
        }

        @Test
        @DisplayName("Should detect admin role correctly")
        void shouldDetectAdminRoleCorrectly() {
            // Given
            UserContext.setUserRoles("ADMIN,CUSTOMER");

            // When & Then
            assertThat(UserContext.isAdmin()).isTrue();
            assertThat(UserContext.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("Should detect customer role correctly")
        void shouldDetectCustomerRoleCorrectly() {
            // Given
            UserContext.setUserRoles("CUSTOMER");

            // When & Then
            assertThat(UserContext.isCustomer()).isTrue();
            assertThat(UserContext.hasRole("CUSTOMER")).isTrue();
            assertThat(UserContext.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should detect restaurant owner role correctly")
        void shouldDetectRestaurantOwnerRoleCorrectly() {
            // Given
            UserContext.setUserRoles("RESTAURANT_OWNER");

            // When & Then
            assertThat(UserContext.isRestaurantOwner()).isTrue();
            assertThat(UserContext.hasRole("RESTAURANT_OWNER")).isTrue();
            assertThat(UserContext.isCustomer()).isFalse();
        }

        @Test
        @DisplayName("Should detect delivery driver role correctly")
        void shouldDetectDeliveryDriverRoleCorrectly() {
            // Given
            UserContext.setUserRoles("DELIVERY_DRIVER");

            // When & Then
            assertThat(UserContext.isDeliveryDriver()).isTrue();
            assertThat(UserContext.hasRole("DELIVERY_DRIVER")).isTrue();
            assertThat(UserContext.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple roles")
        void shouldHandleMultipleRoles() {
            // Given
            UserContext.setUserRoles("ADMIN,RESTAURANT_OWNER,CUSTOMER");

            // When & Then
            assertThat(UserContext.isAdmin()).isTrue();
            assertThat(UserContext.isRestaurantOwner()).isTrue();
            assertThat(UserContext.isCustomer()).isTrue();
            assertThat(UserContext.isDeliveryDriver()).isFalse();
        }

        @Test
        @DisplayName("Should handle null roles gracefully")
        void shouldHandleNullRolesGracefully() {
            // Given
            UserContext.setUserRoles(null);

            // When & Then
            assertThat(UserContext.hasRole("ADMIN")).isFalse();
            assertThat(UserContext.isAdmin()).isFalse();
            assertThat(UserContext.isCustomer()).isFalse();
            assertThat(UserContext.isRestaurantOwner()).isFalse();
            assertThat(UserContext.isDeliveryDriver()).isFalse();
        }

        @Test
        @DisplayName("Should handle empty roles gracefully")
        void shouldHandleEmptyRolesGracefully() {
            // Given
            UserContext.setUserRoles("");

            // When & Then
            assertThat(UserContext.hasRole("ADMIN")).isFalse();
            assertThat(UserContext.isAdmin()).isFalse();
            assertThat(UserContext.isCustomer()).isFalse();
        }

        @Test
        @DisplayName("Should be case sensitive for role checking")
        void shouldBeCaseSensitiveForRoleChecking() {
            // Given
            UserContext.setUserRoles("ADMIN");

            // When & Then
            assertThat(UserContext.hasRole("ADMIN")).isTrue();
            assertThat(UserContext.hasRole("admin")).isFalse();
            assertThat(UserContext.hasRole("Admin")).isFalse();
        }

        @Test
        @DisplayName("Should clear all context values")
        void shouldClearAllContextValues() {
            // Given
            UserContext.setUserId(123L);
            UserContext.setUserEmail("test@example.com");
            UserContext.setUserRoles("CUSTOMER");

            // Verify context is set
            assertThat(UserContext.getUserId()).isEqualTo(123L);
            assertThat(UserContext.getUserEmail()).isEqualTo("test@example.com");
            assertThat(UserContext.getUserRoles()).isEqualTo("CUSTOMER");

            // When
            UserContext.clear();

            // Then
            assertThat(UserContext.getUserId()).isNull();
            assertThat(UserContext.getUserEmail()).isNull();
            assertThat(UserContext.getUserRoles()).isNull();
        }

        @Test
        @DisplayName("Should be thread-safe")
        void shouldBeThreadSafe() throws InterruptedException {
            // Given
            final boolean[] success = { true };

            Thread thread1 = new Thread(() -> {
                try {
                    UserContext.setUserId(1L);
                    UserContext.setUserEmail("user1@example.com");
                    UserContext.setUserRoles("CUSTOMER");

                    Thread.sleep(50);

                    if (!UserContext.getUserId().equals(1L) ||
                            !"user1@example.com".equals(UserContext.getUserEmail()) ||
                            !"CUSTOMER".equals(UserContext.getUserRoles())) {
                        success[0] = false;
                    }

                    UserContext.clear();
                } catch (Exception e) {
                    success[0] = false;
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    UserContext.setUserId(2L);
                    UserContext.setUserEmail("user2@example.com");
                    UserContext.setUserRoles("ADMIN");

                    Thread.sleep(50);

                    if (!UserContext.getUserId().equals(2L) ||
                            !"user2@example.com".equals(UserContext.getUserEmail()) ||
                            !"ADMIN".equals(UserContext.getUserRoles())) {
                        success[0] = false;
                    }

                    UserContext.clear();
                } catch (Exception e) {
                    success[0] = false;
                }
            });

            // When
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            // Then
            assertThat(success[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("UserSecurityConfig Bean Tests")
    class UserSecurityConfigBeanTests {

        @Test
        @DisplayName("Should create UserContextFilter bean")
        void shouldCreateUserContextFilterBean() {
            // Given
            UserSecurityConfig config = new UserSecurityConfig();

            // When
            UserContextFilter filter = config.userContextFilter();

            // Then
            assertThat(filter)
                    .isNotNull()
                    .isInstanceOf(UserContextFilter.class);
        }
    }
}