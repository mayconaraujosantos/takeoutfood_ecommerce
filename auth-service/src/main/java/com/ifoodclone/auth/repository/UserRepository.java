package com.ifoodclone.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.ifoodclone.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se um email já existe
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuários por role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    java.util.List<User> findActiveUsersByRole(@Param("role") User.UserRole role);

    /**
     * Busca usuários ativos
     */
    @Query("SELECT u FROM User u WHERE u.active = true")
    java.util.List<User> findAllActiveUsers();

    /**
     * Busca usuários com email não verificado
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :before")
    java.util.List<User> findUnverifiedUsersCreatedBefore(@Param("before") LocalDateTime before);

    /**
     * Busca usuários bloqueados
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    java.util.List<User> findLockedAccounts();

    /**
     * Atualiza o último login do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Reset tentativas de login falhadas
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Incrementa tentativas de login falhadas
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Bloqueia conta do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true, u.lockedAt = :lockedAt WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("lockedAt") LocalDateTime lockedAt);

    /**
     * Desbloqueia conta do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = false, u.lockedAt = null, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);

    /**
     * Verifica email do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerifiedAt = :verifiedAt WHERE u.id = :userId")
    void verifyEmail(@Param("userId") Long userId, @Param("verifiedAt") LocalDateTime verifiedAt);

    /**
     * Atualiza senha do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, @Param("password") String password,
            @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Desativa usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.active = false, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void deactivateUser(@Param("userId") Long userId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Ativa usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.active = true, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void activateUser(@Param("userId") Long userId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Busca usuários criados em um período
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end ORDER BY u.createdAt DESC")
    java.util.List<User> findUsersCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Conta usuários por role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    Long countActiveUsersByRole(@Param("role") User.UserRole role);

    /**
     * Busca por email ou telefone
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phone = :identifier")
    Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);
}