package com.ifoodclone.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import com.ifoodclone.auth.dto.AuthDto;
import com.ifoodclone.auth.entity.RefreshToken;
import com.ifoodclone.auth.entity.User;
import com.ifoodclone.auth.repository.RefreshTokenRepository;
import com.ifoodclone.auth.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private OpenTelemetry openTelemetry;
    @Mock
    private Tracer tracer;

    private AuthService authService;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Configure OpenTelemetry mock chain
        var spanBuilder = mock(SpanBuilder.class);
        var span = mock(Span.class);
        var scope = mock(Scope.class);

        // Use lenient() to avoid unnecessary stubbing errors for tests that don't use
        // tracing
        Mockito.lenient().when(openTelemetry.getTracer(anyString(), anyString())).thenReturn(tracer);
        Mockito.lenient().when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        Mockito.lenient().when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        Mockito.lenient().when(spanBuilder.startSpan()).thenReturn(span);
        Mockito.lenient().when(span.makeCurrent()).thenReturn(scope);
        Mockito.lenient().when(span.recordException(any(Throwable.class))).thenReturn(span);
        Mockito.lenient().when(span.setStatus(any(), anyString())).thenReturn(span);
        Mockito.lenient().when(span.addEvent(anyString())).thenReturn(span);
        Mockito.lenient().when(span.setAttribute(anyString(), anyString())).thenReturn(span);
        Mockito.lenient().when(span.setAttribute(anyString(), any(Long.class))).thenReturn(span);
        Mockito.lenient().when(span.setAttribute(anyString(), any(Boolean.class))).thenReturn(span);
        authService = new AuthService(
                authenticationManager,
                jwtService,
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                userDetailsService,
                openTelemetry);

        // Set test configurations
        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMs", 900000L);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("encoded-password")
                .role(User.UserRole.CUSTOMER)
                .active(true)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .build();
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        private AuthDto.LoginRequest validLoginRequest;

        @BeforeEach
        void setUp() {
            validLoginRequest = AuthDto.LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .deviceInfo("Test Device")
                    .ipAddress("192.168.1.1")
                    .build();
        }

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // Given
            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(testUser);

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
            when(jwtService.getExpirationTime()).thenReturn(60000L);

            // When
            AuthDto.LoginResponse response = authService.login(validLoginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

            verify(refreshTokenRepository).save(any(RefreshToken.class));
            verify(userRepository).updateLastLoginTime(eq(1L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should reset failed login attempts after successful login")
        void shouldResetFailedLoginAttemptsAfterSuccessfulLogin() {
            // Given
            testUser.setFailedLoginAttempts(3);
            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(testUser);

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
            when(jwtService.getExpirationTime()).thenReturn(60000L);

            // When
            authService.login(validLoginRequest);

            // Then
            verify(userRepository).resetFailedLoginAttempts(1L);
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        void shouldThrowExceptionForNonExistentUser() {
            // Given
            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(validLoginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Credenciais inválidas");
        }

        @Test
        @DisplayName("Should throw exception for locked account")
        void shouldThrowExceptionForLockedAccount() {
            // Given
            testUser.setAccountLocked(true);
            testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.login(validLoginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Conta temporariamente bloqueada devido a muitas tentativas de login");
        }

        @Test
        @DisplayName("Should throw exception for inactive account")
        void shouldThrowExceptionForInactiveAccount() {
            // Given
            testUser.setActive(false);

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.login(validLoginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Conta inativa");
        }

        @Test
        @DisplayName("Should increment failed attempts on authentication failure")
        void shouldIncrementFailedAttemptsOnAuthenticationFailure() {
            // Given
            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(validLoginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Credenciais inválidas");

            verify(userRepository).incrementFailedLoginAttempts(1L);
        }

        @Test
        @DisplayName("Should lock account after max failed attempts")
        void shouldLockAccountAfterMaxFailedAttempts() {
            // Given
            testUser.setFailedLoginAttempts(4); // Next failure will reach max (5)

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(validLoginRequest))
                    .isInstanceOf(RuntimeException.class);

            verify(userRepository).incrementFailedLoginAttempts(1L);
            verify(userRepository).lockAccount(eq(1L), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        private AuthDto.RegisterRequest validRegisterRequest;

        @BeforeEach
        void setUp() {
            validRegisterRequest = AuthDto.RegisterRequest.builder()
                    .email("newuser@example.com")
                    .password("password123")
                    .firstName("New")
                    .lastName("User")
                    .phone("+5511999999999")
                    .role(User.UserRole.CUSTOMER)
                    .build();
        }

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() {
            // Given
            User savedUser = User.builder()
                    .id(2L)
                    .email("newuser@example.com")
                    .firstName("New")
                    .lastName("User")
                    .role(User.UserRole.CUSTOMER)
                    .active(true)
                    .emailVerified(false)
                    .build();
            when(userRepository.existsByEmail(validRegisterRequest.getEmail()))
                    .thenReturn(false);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("encoded-password");
            when(userRepository.save(any(User.class)))
                    .thenReturn(savedUser);

            // When
            AuthDto.UserInfo userInfo = authService.register(validRegisterRequest);

            // Then
            assertThat(userInfo).isNotNull();
            assertThat(userInfo.getEmail()).isEqualTo("newuser@example.com");
            assertThat(userInfo.getFirstName()).isEqualTo("New");
            assertThat(userInfo.getLastName()).isEqualTo("User");
            assertThat(userInfo.getRole()).isEqualTo(User.UserRole.CUSTOMER);
            assertThat(userInfo.getActive()).isTrue();
            assertThat(userInfo.getEmailVerified()).isFalse();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo("newuser@example.com");
            assertThat(capturedUser.getPassword()).isEqualTo("encoded-password");
            assertThat(capturedUser.getActive()).isTrue();
            assertThat(capturedUser.getEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception for existing email")
        void shouldThrowExceptionForExistingEmail() {
            // Given
            when(userRepository.existsByEmail(validRegisterRequest.getEmail()))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email já cadastrado");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        private AuthDto.RefreshTokenRequest validRefreshRequest;
        private RefreshToken validRefreshToken;

        @BeforeEach
        void setUp() {
            validRefreshRequest = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();

            validRefreshToken = RefreshToken.builder()
                    .id(1L)
                    .token("valid-refresh-token")
                    .user(testUser)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revoked(false)
                    .build();
        }

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // Given
            when(jwtService.isTokenValid("valid-refresh-token")).thenReturn(true);
            when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
            when(refreshTokenRepository.findByToken("valid-refresh-token"))
                    .thenReturn(Optional.of(validRefreshToken));
            when(jwtService.generateToken(testUser)).thenReturn("new-access-token");
            when(jwtService.getExpirationTime()).thenReturn(60000L);

            // When
            AuthDto.TokenResponse response = authService.refreshToken(validRefreshRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
            assertThat(response.getExpiresIn()).isEqualTo(60L);

            verify(refreshTokenRepository).save(validRefreshToken);
            assertThat(validRefreshToken.isUsed()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for invalid refresh token")
        void shouldThrowExceptionForInvalidRefreshToken() {
            // Given
            when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

            AuthDto.RefreshTokenRequest request = AuthDto.RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Refresh token inválido");
        }

        @Test
        @DisplayName("Should throw exception for revoked refresh token")
        void shouldThrowExceptionForRevokedRefreshToken() {
            // Given
            validRefreshToken.setRevoked(true);

            when(jwtService.isTokenValid("valid-refresh-token")).thenReturn(true);
            when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
            when(refreshTokenRepository.findByToken("valid-refresh-token"))
                    .thenReturn(Optional.of(validRefreshToken));

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(validRefreshRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Refresh token foi revogado");
        }

        @Test
        @DisplayName("Should throw exception for expired refresh token")
        void shouldThrowExceptionForExpiredRefreshToken() {
            // Given
            validRefreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));

            when(jwtService.isTokenValid("valid-refresh-token")).thenReturn(true);
            when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
            when(refreshTokenRepository.findByToken("valid-refresh-token"))
                    .thenReturn(Optional.of(validRefreshToken));

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(validRefreshRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Refresh token expirado");
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() {
            // Given
            String refreshToken = "valid-refresh-token";

            // When
            authService.logout(refreshToken);

            // Then
            verify(refreshTokenRepository).revokeToken(eq(refreshToken), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle null refresh token gracefully")
        void shouldHandleNullRefreshTokenGracefully() {
            // When
            authService.logout(null);

            // Then
            verify(refreshTokenRepository, never()).revokeToken(any(), any());
        }

        @Test
        @DisplayName("Should logout from all devices")
        void shouldLogoutFromAllDevices() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            authService.logoutFromAllDevices(1L);

            // Then
            verify(refreshTokenRepository).revokeAllUserTokens(eq(testUser), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should throw exception for non-existent user in logout all")
        void shouldThrowExceptionForNonExistentUserInLogoutAll() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.logoutFromAllDevices(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Usuário não encontrado");
        }
    }

    @Nested
    @DisplayName("Password Change Tests")
    class PasswordChangeTests {

        private AuthDto.ChangePasswordRequest validChangeRequest;

        @BeforeEach
        void setUp() {
            validChangeRequest = AuthDto.ChangePasswordRequest.builder()
                    .currentPassword("current-password")
                    .newPassword("new-password")
                    .build();
        }

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("current-password", testUser.getPassword()))
                    .thenReturn(true);
            when(passwordEncoder.encode("new-password")).thenReturn("new-encoded-password");

            // When
            authService.changePassword(1L, validChangeRequest);

            // Then
            verify(userRepository).updatePassword(eq(1L), eq("new-encoded-password"), any(LocalDateTime.class));
            verify(refreshTokenRepository).revokeAllUserTokens(eq(testUser), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should throw exception for incorrect current password")
        void shouldThrowExceptionForIncorrectCurrentPassword() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("current-password", testUser.getPassword()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.changePassword(1L, validChangeRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Senha atual incorreta");

            verify(userRepository, never()).updatePassword(any(), any(), any());
            verify(refreshTokenRepository, never()).revokeAllUserTokens(any(), any());
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        void shouldThrowExceptionForNonExistentUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.changePassword(1L, validChangeRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Usuário não encontrado");
        }
    }
}