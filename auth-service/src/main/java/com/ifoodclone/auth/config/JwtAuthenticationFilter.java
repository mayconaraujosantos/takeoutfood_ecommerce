package com.ifoodclone.auth.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ifoodclone.auth.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    try {
      String token = extractTokenFromRequest(request);

      if (token != null && jwtService.isTokenValid(token)) {
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          if (jwtService.isTokenValid(token, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Cannot set user authentication: {}", ex);
    }

    filterChain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    return null;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();

    // Não aplicar o filtro para endpoints públicos
    return path.equals("/api/v1/auth/login") ||
        path.equals("/api/v1/auth/register") ||
        path.equals("/api/v1/auth/refresh") ||
        path.equals("/api/v1/auth/password/reset") ||
        path.equals("/api/v1/auth/password/reset/confirm") ||
        path.equals("/api/v1/auth/email/verify") ||
        path.equals("/api/v1/auth/health") ||
        path.startsWith("/actuator/") ||
        path.startsWith("/api/dev/") ||
        path.startsWith("/api/test/") ||
        path.startsWith("/api/tracing/") ||
        path.startsWith("/swagger-ui") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/swagger-resources");
  }
}