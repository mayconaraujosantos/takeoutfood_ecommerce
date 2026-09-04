package com.ifoodclone.auth.service;

import java.time.LocalDateTime;

import com.ifoodclone.auth.dto.AuthDto;
import com.ifoodclone.auth.entity.RefreshToken;
import com.ifoodclone.auth.entity.User;
import com.ifoodclone.auth.repository.RefreshTokenRepository;
import com.ifoodclone.auth.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String SERVICE_NAME = "auth-service";
    private static final String SERVICE_VERSION = "1.0.0";
    private final Tracer tracer;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.security.account.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.account.lockout-duration:900000}")
    private long lockoutDurationMs;

    public AuthService(AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService,
            OpenTelemetry openTelemetry) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.tracer = openTelemetry.getTracer(SERVICE_NAME, SERVICE_VERSION);
    }

    /**
     * Autenticar usuário e gerar tokens
     */
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        Span span = tracer.spanBuilder("auth.service.login")
                .setAttribute("service.name", SERVICE_NAME)
                .setAttribute("operation", "user.authentication")
                .setAttribute("user.email", request.getEmail())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.addEvent("authentication.started");

            // Log estruturado
            logger.info("Iniciando autenticação para usuário: {}", request.getEmail());

            // Verificar se o usuário existe e está ativo
            span.addEvent("user.lookup.started");
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

            span.addEvent("user.validation.started")
                    .setAttribute("user.id", user.getId())
                    .setAttribute("user.active", user.getActive())
                    .setAttribute("user.locked", !user.isAccountNonLocked());

            // Verificar se a conta não está bloqueada
            if (!user.isAccountNonLocked()) {
                span.setStatus(StatusCode.ERROR, "Account locked")
                        .addEvent("authentication.failed.account_locked");
                throw new RuntimeException("Conta temporariamente bloqueada devido a muitas tentativas de login");
            }

            // Verificar se a conta está ativa
            if (!user.getActive()) {
                span.setStatus(StatusCode.ERROR, "Account inactive")
                        .addEvent("authentication.failed.account_inactive");
                throw new RuntimeException("Conta inativa");
            }

            // Tentar autenticar
            span.addEvent("credential.validation.started");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User authenticatedUser = (User) userDetails;

            span.addEvent("authentication.successful")
                    .setAttribute("user.role", authenticatedUser.getRole().toString());

            // Reset tentativas de login após sucesso
            if (authenticatedUser.getFailedLoginAttempts() > 0) {
                span.addEvent("failed.attempts.reset")
                        .setAttribute("previous.failed.attempts", authenticatedUser.getFailedLoginAttempts());
                userRepository.resetFailedLoginAttempts(authenticatedUser.getId());
            }

            // Atualizar último login
            userRepository.updateLastLoginTime(authenticatedUser.getId(), LocalDateTime.now());

            // Gerar tokens
            span.addEvent("token.generation.started");
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Salvar refresh token
            saveRefreshToken(authenticatedUser, refreshToken, request.getDeviceInfo(), request.getIpAddress());

            span.addEvent("login.completed")
                    .setAttribute("result.status", "success");
            span.setStatus(StatusCode.OK);

            logger.info("Usuário autenticado com sucesso: {} (ID: {})", request.getEmail(), authenticatedUser.getId());

            // Criar resposta
            return AuthDto.LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.getExpirationTime() / 1000) // converter para segundos
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusNanos(jwtService.getExpirationTime() * 1_000_000))
                    .user(buildUserInfo(authenticatedUser))
                    .build();

        } catch (AuthenticationException ex) {
            span.recordException(ex)
                    .setStatus(StatusCode.ERROR, "Authentication failed: " + ex.getMessage())
                    .addEvent("authentication.failed.invalid_credentials")
                    .setAttribute("error.type", ex.getClass().getSimpleName());

            // Incrementar tentativas de login falhadas
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                userRepository.incrementFailedLoginAttempts(user.getId());

                // Verificar se deve bloquear a conta
                if (user.getFailedLoginAttempts() + 1 >= maxLoginAttempts) {
                    span.addEvent("account.locked")
                            .setAttribute("failed.attempts", user.getFailedLoginAttempts() + 1);
                    userRepository.lockAccount(user.getId(), LocalDateTime.now());
                }
            });

            logger.warn("Falha na autenticação para usuário: {} - {}", request.getEmail(), ex.getMessage());
            throw new RuntimeException("Credenciais inválidas");

        } catch (Exception e) {
            span.recordException(e)
                    .setStatus(StatusCode.ERROR, "Login failed: " + e.getMessage())
                    .addEvent("login.failed.unexpected_error")
                    .setAttribute("error.type", e.getClass().getSimpleName());

            logger.error("Erro inesperado durante login para usuário: {}", request.getEmail(), e);
            throw e;

        } finally {
            span.end();
        }
    }

    /**
     * Registrar novo usuário
     */
    public AuthDto.UserInfo register(AuthDto.RegisterRequest request) {
        logger.info("Iniciando processo de registro - Email: {}, Role: {}",
                request.getEmail(), request.getRole());

        // Verificar se o email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Tentativa de registro com email já existente: {}", request.getEmail());
            throw new RuntimeException("Email já cadastrado");
        }

        logger.debug("Email disponível para registro: {}", request.getEmail());

        // Definir role padrão para registros públicos: sempre CUSTOMER.
        // Ignorar role enviada pelo cliente para evitar elevação de privilégio.
        User.UserRole role = User.UserRole.CUSTOMER;
        logger.debug("Role definida como CUSTOMER para registro público");

        // Criar novo usuário (role pública forçada para CUSTOMER)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        logger.debug("Salvando usuário no banco de dados: {}", request.getEmail());
        user = userRepository.save(user);

        logger.info("Usuário registrado com sucesso - ID: {}, Email: {}, UserType: {}",
                user.getId(), user.getEmail(), user.getRole());

        return buildUserInfo(user);
    }

    /**
     * Renovar access token usando refresh token
     */
    public AuthDto.TokenResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        // Validar refresh token
        if (!jwtService.isTokenValid(refreshTokenValue) || !jwtService.isRefreshToken(refreshTokenValue)) {
            throw new RuntimeException("Refresh token inválido");
        }

        // Buscar refresh token no banco
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token não encontrado"));

        // Verificar se não foi revogado
        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token foi revogado");
        }

        // Verificar se não expirou
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expirado");
        }

        // Buscar usuário
        User user = refreshToken.getUser();
        if (!user.getActive()) {
            throw new RuntimeException("Usuário inativo");
        }

        // Gerar novo access token
        String newAccessToken = jwtService.generateToken(user);

        // Marcar token como usado
        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        return AuthDto.TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue) // Manter o mesmo refresh token
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusNanos(jwtService.getExpirationTime() * 1_000_000))
                .build();
    }

    /**
     * Logout - revogar refresh token
     */
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null) {
            refreshTokenRepository.revokeToken(refreshTokenValue, LocalDateTime.now());
        }
    }

    /**
     * Logout de todos os dispositivos
     */
    public void logoutFromAllDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
    }

    /**
     * Trocar senha
     */
    public void changePassword(Long userId, AuthDto.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar senha atual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        // Atualizar senha
        userRepository.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()), LocalDateTime.now());

        // Revogar todos os tokens do usuário
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
    }

    /**
     * Salvar refresh token
     */
    private void saveRefreshToken(User user, String tokenValue, String deviceInfo, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusNanos(jwtService.getRefreshExpirationTime() * 1_000_000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Construir informações do usuário
     */
    private AuthDto.UserInfo buildUserInfo(User user) {
        return AuthDto.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .roleDisplayName(user.getRole().getDisplayName())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .active(user.getActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Get user by ID
     */
    public AuthDto.UserInfo getUserById(Long userId) {
        Span span = tracer.spanBuilder("auth.getUserById")
                .setAttribute("service.name", SERVICE_NAME)
                .setAttribute("operation", "user.get_by_id")
                .setAttribute("user.id", userId.toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            logger.debug("Buscando usuário por ID: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            span.addEvent("user.found")
                    .setAttribute("user.email", user.getEmail())
                    .setAttribute("user.role", user.getRole().toString());
            span.setStatus(StatusCode.OK);

            return buildUserInfo(user);

        } catch (Exception ex) {
            span.recordException(ex)
                    .setStatus(StatusCode.ERROR, "Get user by ID failed: " + ex.getMessage());
            logger.error("Erro ao buscar usuário por ID: {}", userId, ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}