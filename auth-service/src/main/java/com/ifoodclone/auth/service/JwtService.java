package com.ifoodclone.auth.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpirationMs, "ACCESS");
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails, refreshExpirationMs, "REFRESH");
    }

    private String buildToken(Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration,
            String tokenType) {

        // Adicionar claims personalizados
        extraClaims.put("type", tokenType);

        if (userDetails instanceof com.ifoodclone.auth.entity.User) {
            com.ifoodclone.auth.entity.User user = (com.ifoodclone.auth.entity.User) userDetails;
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("emailVerified", user.getEmailVerified());
        }

        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isRefreshToken(String token) {
        String tokenType = extractTokenType(token);
        return "REFRESH".equals(tokenType);
    }

    public boolean isAccessToken(String token) {
        String tokenType = extractTokenType(token);
        return "ACCESS".equals(tokenType);
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    public long getRefreshExpirationTime() {
        return refreshExpirationMs;
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Valida se o token é do tipo correto
     */
    public boolean validateTokenType(String token, String expectedType) {
        try {
            String tokenType = extractTokenType(token);
            return expectedType.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém o tempo restante do token em segundos
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        return Math.max(0, (expiration.getTime() - now.getTime()) / 1000);
    }

    /**
     * Verifica se o token expira em breve (menos que o tempo especificado)
     */
    public boolean isTokenExpiringSoon(String token, long thresholdSeconds) {
        long remaining = getTokenRemainingTime(token);
        return remaining > 0 && remaining < thresholdSeconds;
    }

    /**
     * Gera token com expiração personalizada para desenvolvimento
     */
    public String generateLongLivedToken(Map<String, Object> extraClaims, int validityDays) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject((String) extraClaims.get("sub"))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (validityDays * 24 * 60 * 60 * 1000L)))
                .signWith(getSignKey())
                .compact();
    }
}