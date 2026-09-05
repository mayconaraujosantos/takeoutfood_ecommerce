package com.ifoodclone.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifoodclone.auth.config.TestConfig;
import com.ifoodclone.auth.dto.AuthDto;
import com.ifoodclone.auth.entity.User;
import com.ifoodclone.auth.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// addFilters = false: this slice isolates AuthController's request-mapping/serialization
// logic with a mocked AuthService; the security filter chain (JWT parsing, CSRF,
// authorization rules) has its own coverage in AuthenticationIntegrationTest. Without this,
// SecurityConfig - a plain @Configuration bean, not a Filter/HandlerInterceptor - isn't
// picked up by the @WebMvcTest slice at all, so requests would run under Spring Boot's
// default deny-all/CSRF-enabled security instead of the app's real rules.
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Autowired
    private UserDetailsService userDetailsService;

    private AuthDto.LoginResponse loginResponse;
    private AuthDto.UserInfo userInfo;
    private AuthDto.TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        userInfo = AuthDto.UserInfo.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.CUSTOMER)
                .active(true)
                .emailVerified(true)
                .build();

        loginResponse = AuthDto.LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .user(userInfo)
                .build();

        tokenResponse = AuthDto.TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .build();

        // getCurrentUserId() in AuthController requires the security principal to be a
        // com.ifoodclone.auth.entity.User (not the generic UserDetails @WithMockUser
        // would produce), so @WithUserDetails is used below, backed by this stub.
        User authenticatedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded-password")
                .role(User.UserRole.CUSTOMER)
                .active(true)
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(authenticatedUser);
    }

    @Nested
    @DisplayName("User Registration")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            AuthDto.RegisterRequest request = AuthDto.RegisterRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .firstName("Test")
                    .lastName("User")
                    .role(User.UserRole.CUSTOMER)
                    .build();

            when(authService.register(any(AuthDto.RegisterRequest.class)))
                    .thenReturn(userInfo);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.firstName").value("Test"))
                    .andExpect(jsonPath("$.data.lastName").value("User"));
        }

        @Test
        @DisplayName("Should return error when registration fails")
        void shouldReturnErrorWhenRegistrationFails() throws Exception {
            // Given
            AuthDto.RegisterRequest request = AuthDto.RegisterRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .firstName("Test")
                    .lastName("User")
                    .role(User.UserRole.CUSTOMER)
                    .build();

            when(authService.register(any(AuthDto.RegisterRequest.class)))
                    .thenThrow(new RuntimeException("Email já cadastrado"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Falha no registro"));
        }

        @Test
        @DisplayName("Should return validation error for invalid request")
        void shouldReturnValidationErrorForInvalidRequest() throws Exception {
            // Given - Invalid request with missing required fields
            AuthDto.RegisterRequest request = AuthDto.RegisterRequest.builder()
                    .email("invalid-email")
                    .password("123") // Too short
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("User Authentication")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void shouldAuthenticateUserSuccessfully() throws Exception {
            // Given
            AuthDto.LoginRequest request = AuthDto.LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(authService.login(any(AuthDto.LoginRequest.class)))
                    .thenReturn(loginResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("Should return error when authentication fails")
        void shouldReturnErrorWhenAuthenticationFails() throws Exception {
            // Given
            AuthDto.LoginRequest request = AuthDto.LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongpassword")
                    .build();

            when(authService.login(any(AuthDto.LoginRequest.class)))
                    .thenThrow(new RuntimeException("Credenciais inválidas"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Falha no login"));
        }
    }

    @Nested
    @DisplayName("Token Refresh")
    class TokenRefreshTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() throws Exception {
            // Given
            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("refresh-token")
                    .build();

            when(authService.refreshToken(any(AuthDto.RefreshTokenRequest.class)))
                    .thenReturn(tokenResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
        }

        @Test
        @DisplayName("Should return error when refresh token is invalid")
        void shouldReturnErrorWhenRefreshTokenIsInvalid() throws Exception {
            // Given
            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            when(authService.refreshToken(any(AuthDto.RefreshTokenRequest.class)))
                    .thenThrow(new RuntimeException("Refresh token inválido"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Falha ao renovar token"));
        }
    }

    @Nested
    @DisplayName("User Profile")
    class UserProfileTests {

        @Test
        @DisplayName("Should get user profile successfully")
        @WithUserDetails("test@example.com")
        void shouldGetUserProfileSuccessfully() throws Exception {
            // Given
            when(authService.getUserById(anyLong())).thenReturn(userInfo);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/profile")
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("Should return error when user not found")
        @WithUserDetails("test@example.com")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            // Given
            when(authService.getUserById(anyLong()))
                    .thenThrow(new RuntimeException("Usuário não encontrado"));

            // When & Then
            mockMvc.perform(get("/api/v1/auth/profile")
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Falha ao recuperar perfil"));
        }
    }

    @Nested
    @DisplayName("Logout Operations")
    class LogoutOperationsTests {

        @Test
        @DisplayName("Should logout successfully")
        @WithUserDetails("test@example.com")
        void shouldLogoutSuccessfully() throws Exception {
            // Given
            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("refresh-token")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logout realizado com sucesso"));
        }

        @Test
        @DisplayName("Should return error when logout fails")
        @WithUserDetails("test@example.com")
        void shouldReturnErrorWhenLogoutFails() throws Exception {
            // Given
            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            doThrow(new RuntimeException("Token inválido"))
                    .when(authService).logout(any());

            // When & Then
            mockMvc.perform(post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Falha no logout"));
        }
    }

    @Nested
    @DisplayName("Password Reset and Email Verification")
    class PasswordResetAndEmailVerificationTests {

        // Regression coverage for a real bug: AuthDto.PasswordResetRequest and
        // EmailVerificationRequest each have a single field, and a @Data @Builder class
        // with no explicit constructor has no public constructor Jackson can use to
        // deserialize a raw JSON body (only serialization, via the builder, worked before
        // the fix) -- these tests post real JSON, unlike the builder-object round-trips
        // above, so they actually exercise deserialization of the request body.

        @Test
        @DisplayName("POST /password/reset deserializes a single-field body and returns success")
        void shouldRequestPasswordReset() throws Exception {
            AuthDto.PasswordResetRequest request = AuthDto.PasswordResetRequest.builder()
                    .email("test@example.com")
                    .build();

            mockMvc.perform(post("/api/v1/auth/password/reset")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("POST /password/reset/confirm deserializes the body and returns success")
        void shouldConfirmPasswordReset() throws Exception {
            AuthDto.PasswordResetConfirmRequest request = AuthDto.PasswordResetConfirmRequest.builder()
                    .token("reset-token")
                    .newPassword("new-password123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/password/reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("POST /password/reset/confirm returns error for an invalid token")
        void shouldReturnErrorForInvalidResetToken() throws Exception {
            AuthDto.PasswordResetConfirmRequest request = AuthDto.PasswordResetConfirmRequest.builder()
                    .token("bad-token")
                    .newPassword("new-password123")
                    .build();

            doThrow(new RuntimeException("Token inválido"))
                    .when(authService).confirmPasswordReset(anyString(), anyString());

            mockMvc.perform(post("/api/v1/auth/password/reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /email/verify deserializes a single-field body and returns success")
        void shouldVerifyEmail() throws Exception {
            AuthDto.EmailVerificationRequest request = AuthDto.EmailVerificationRequest.builder()
                    .token("verify-token")
                    .build();

            mockMvc.perform(post("/api/v1/auth/email/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("Health Check")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return health status")
        void shouldReturnHealthStatus() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/auth/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Auth service is running"))
                    .andExpect(jsonPath("$.data").value("OK"));
        }
    }
}