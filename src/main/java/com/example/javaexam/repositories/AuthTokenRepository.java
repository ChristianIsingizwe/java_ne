package com.example.javaexam.repositories;

import com.example.javaexam.models.AuthToken;
import com.example.javaexam.models.enums.TokenType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTypeAndToken(TokenType type, String token);

    boolean existsByTypeAndToken(TokenType type, String token);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}
