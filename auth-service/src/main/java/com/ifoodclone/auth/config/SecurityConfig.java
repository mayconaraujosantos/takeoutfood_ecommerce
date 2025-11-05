package com.ifoodclone.auth.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(UserDetailsService userDetailsService,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        // Spring Security 6.5+ modern approach - configure authentication inline
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(auth -> auth
            // ===== ENDPOINTS PÚBLICOS - MÁXIMA PRIORIDADE =====

            // Health checks - Monitoramento
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/api/v1/auth/health").permitAll()

            // Authentication endpoints - Core public APIs
            .requestMatchers(
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/auth/refresh",
                "/api/v1/auth/password/reset",
                "/api/v1/auth/password/reset/confirm",
                "/api/v1/auth/email/verify")
            .permitAll()

            // Development & Testing endpoints
            .requestMatchers("/api/dev/**", "/api/test/**", "/api/tracing/**").permitAll()

            // Documentation endpoints
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

            // Admin endpoints - Profissional API v1
            .requestMatchers("/api/v1/auth/admin/**").hasRole("ADMIN")

            // Restaurant owner endpoints - Profissional API v1
            .requestMatchers("/api/v1/auth/restaurant/**").hasAnyRole("ADMIN", "RESTAURANT_OWNER")

            // Delivery endpoints - Profissional API v1
            .requestMatchers("/api/v1/auth/delivery/**").hasAnyRole("ADMIN", "DELIVERY_PERSON")

            // User endpoints - Profissional API v1
            .requestMatchers("/api/v1/auth/user/**")
            .hasAnyRole("ADMIN", "CUSTOMER", "RESTAURANT_OWNER", "DELIVERY_PERSON")

            // Profile endpoints - qualquer usuário autenticado - Profissional API v1
            .requestMatchers("/api/v1/auth/profile/**").authenticated()

            // Outros endpoints requerem autenticação
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Permitir origens específicas (em produção, configurar adequadamente)
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));

    // Métodos HTTP permitidos
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // Headers permitidos
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // Permitir credenciais
    configuration.setAllowCredentials(true);

    // Headers expostos
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));

    // Tempo de cache para preflight requests
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}