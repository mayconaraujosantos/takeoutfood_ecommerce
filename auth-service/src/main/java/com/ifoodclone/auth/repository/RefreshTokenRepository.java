package com.ifoodclone.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.ifoodclone.auth.entity.RefreshToken;
import com.ifoodclone.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca refresh token por token
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca tokens do usuário
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findActiveTokensByUser(@Param("user") User user);

    /**
     * Busca todos os tokens do usuário (ativos e revogados)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user ORDER BY rt.createdAt DESC")
    List<RefreshToken> findAllTokensByUser(@Param("user") User user);

    /**
     * Busca tokens por device
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.deviceInfo = :deviceInfo AND rt.revoked = false")
    List<RefreshToken> findActiveTokensByUserAndDevice(@Param("user") User user,
            @Param("deviceInfo") String deviceInfo);

    /**
     * Busca tokens expirados
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now AND rt.revoked = false")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Busca tokens que expiram em breve
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt BETWEEN :now AND :threshold AND rt.revoked = false")
    List<RefreshToken> findTokensExpiringBetween(@Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold);

    /**
     * Revoga todos os tokens do usuário
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.revoked = false")
    int revokeAllUserTokens(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Revoga token específico
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.token = :token")
    int revokeToken(@Param("token") String token, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Revoga tokens do device específico
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.deviceInfo = :deviceInfo AND rt.revoked = false")
    int revokeTokensByDevice(@Param("user") User user, @Param("deviceInfo") String deviceInfo,
            @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Remove tokens expirados há mais de X dias
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :threshold")
    int deleteExpiredTokensOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Remove tokens revogados há mais de X dias
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :threshold")
    int deleteRevokedTokensOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Conta tokens ativos do usuário
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    Long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Conta tokens por device
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.deviceInfo = :deviceInfo AND rt.revoked = false AND rt.expiresAt > :now")
    Long countActiveTokensByUserAndDevice(@Param("user") User user, @Param("deviceInfo") String deviceInfo,
            @Param("now") LocalDateTime now);

    /**
     * Busca tokens por IP
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.ipAddress = :ipAddress AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByIpAddress(@Param("ipAddress") String ipAddress,
            @Param("now") LocalDateTime now);

    /**
     * Verifica se existe token ativo para o usuário
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    Boolean hasActiveTokens(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Busca último token usado pelo usuário
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false ORDER BY rt.usedAt DESC")
    Optional<RefreshToken> findLastUsedTokenByUser(@Param("user") User user);

    /**
     * Atualiza último uso do token
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.usedAt = :usedAt WHERE rt.id = :tokenId")
    void updateLastUsedAt(@Param("tokenId") Long tokenId, @Param("usedAt") LocalDateTime usedAt);

    /**
     * Busca estatísticas de tokens por período
     */
    @Query("SELECT DATE(rt.createdAt) as date, COUNT(rt) as count FROM RefreshToken rt WHERE rt.createdAt BETWEEN :start AND :end GROUP BY DATE(rt.createdAt) ORDER BY date")
    List<Object[]> getTokenStatsByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}