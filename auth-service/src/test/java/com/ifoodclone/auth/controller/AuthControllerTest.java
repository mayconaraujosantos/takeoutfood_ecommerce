package com.ifoodclone.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ifoodclone.auth.config.JwtAuthenticationEntryPoint;
import com.ifoodclone.auth.config.JwtAuthenticationFilter;

@WebMvcTest(controllers = AuthController.class)
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

        @MockBean
        private UserDetailsService userDetailsService;

        @MockBean
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private AuthenticationManager authenticationManager;

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
        void shouldGetUserProfileSuccessfully() throws Exception {
            // Given
            when(authService.getUserById(anyLong())).thenReturn(userInfo);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/profile")
                                        .with(authentication(buildUserAuthentication()))
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("Should return error when user not found")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            // Given
            when(authService.getUserById(anyLong()))
                    .thenThrow(new RuntimeException("Usuário não encontrado"));

            // When & Then
            mockMvc.perform(get("/api/v1/auth/profile")
                    .with(authentication(buildUserAuthentication()))
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
        void shouldLogoutSuccessfully() throws Exception {
            // Given
            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("refresh-token")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/logout")
                    .with(authentication(buildUserAuthentication()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logout realizado com sucesso"));
        }

        @Test
        @DisplayName("Should return error when logout fails")
        void shouldReturnErrorWhenLogoutFails() throws Exception {
            // Given
            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            doThrow(new RuntimeException("Token inválido"))
                    .when(authService).logout(any());

            // When & Then
            mockMvc.perform(post("/api/v1/auth/logout")
                    .with(authentication(buildUserAuthentication()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Falha no logout"));
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

        private Authentication buildUserAuthentication() {
                User principal = User.builder()
                                .id(1L)
                                .email("test@example.com")
                                .password("encoded-password")
                                .firstName("Test")
                                .lastName("User")
                                .role(User.UserRole.CUSTOMER)
                                .active(true)
                                .emailVerified(true)
                                .build();
                return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        }
}