package com.ifoodclone.auth.service;

import java.time.LocalDateTime;

import com.ifoodclone.auth.dto.AuthDto;
import com.ifoodclone.auth.entity.RefreshToken;
import com.ifoodclone.auth.entity.User;
import com.ifoodclone.auth.repository.RefreshTokenRepository;
import com.ifoodclone.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

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
            CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Autenticar usuário e gerar tokens
     */
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        try {
            // Verificar se o usuário existe e está ativo
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

            // Verificar se a conta não está bloqueada
            if (!user.isAccountNonLocked()) {
                throw new RuntimeException("Conta temporariamente bloqueada devido a muitas tentativas de login");
            }

            // Verificar se a conta está ativa
            if (!user.getActive()) {
                throw new RuntimeException("Conta inativa");
            }

            // Tentar autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User authenticatedUser = (User) userDetails;

            // Reset tentativas de login após sucesso
            if (authenticatedUser.getFailedLoginAttempts() > 0) {
                userRepository.resetFailedLoginAttempts(authenticatedUser.getId());
            }

            // Atualizar último login
            userRepository.updateLastLoginTime(authenticatedUser.getId(), LocalDateTime.now());

            // Gerar tokens
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Salvar refresh token
            saveRefreshToken(authenticatedUser, refreshToken, request.getDeviceInfo(), request.getIpAddress());

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
            // Incrementar tentativas de login falhadas
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                userRepository.incrementFailedLoginAttempts(user.getId());

                // Verificar se deve bloquear a conta
                if (user.getFailedLoginAttempts() + 1 >= maxLoginAttempts) {
                    userRepository.lockAccount(user.getId(), LocalDateTime.now());
                }
            });

            throw new RuntimeException("Credenciais inválidas");
        }
    }

    /**
     * Registrar novo usuário
     */
    public AuthDto.UserInfo register(AuthDto.RegisterRequest request) {
        // Verificar se o email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Criar novo usuário
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

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
}