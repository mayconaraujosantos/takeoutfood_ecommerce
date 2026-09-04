package com.ifoodclone.auth.repository;

import java.util.Optional;

import com.ifoodclone.auth.entity.VerificationToken;
import com.ifoodclone.auth.entity.VerificationToken.TokenType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);
}
