package com.example.javaexam.services;

import com.example.javaexam.models.RevokedToken;
import com.example.javaexam.repositories.RevokedTokenRepository;
import com.example.javaexam.security.JwtService;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    public void revoke(String rawToken, Claims claims) {
        String tokenHash = jwtService.hashToken(rawToken);
        if (revokedTokenRepository.existsById(tokenHash)) {
            return;
        }

        revokedTokenRepository.save(RevokedToken.builder()
                .tokenHash(tokenHash)
                .subject(claims.getSubject())
                .expiresAt(LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneOffset.UTC))
                .revokedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }

    public boolean isRevoked(String rawToken) {
        return revokedTokenRepository.existsById(jwtService.hashToken(rawToken));
    }
}
