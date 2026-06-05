package com.example.javaexam.services;

import com.example.javaexam.security.dtos.AuthResponse;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.models.AuthToken;
import com.example.javaexam.models.User;
import com.example.javaexam.models.enums.TokenType;
import com.example.javaexam.repositories.AuthTokenRepository;
import com.example.javaexam.repositories.UserRepository;
import com.example.javaexam.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues access/refresh token pairs and manages refresh-token lifecycle:
 * rotation, reuse detection, and single-device revocation (logout).
 *
 * <p>Access tokens are stateless. Refresh tokens are stateless too, but each
 * carries a unique {@code jti}; rotated/revoked ids are remembered in the
 * {@link AuthTokenRepository} denylist so a replayed token can be
 * detected and a single device can be logged out.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    /** Builds a fresh access + refresh token pair for the given user. */
    public AuthResponse issueTokens(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.getAccessExpirationMs(),
                jwtService.getRefreshExpirationMs(),
                user.getEmail(),
                user.getRole());
    }

    /**
     * Exchanges a refresh token for a new pair, rotating (and revoking) the old
     * one. If a refresh token that was already rotated/revoked is replayed, it
     * is treated as theft: every session for that user is invalidated.
     *
     * <p>{@code noRollbackFor} is essential: the reuse-detection path bumps the
     * token version (revoking all sessions) and then throws - without this, the
     * rollback would undo that security action.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse refresh(String refreshToken) {
        Claims claims = parseRefreshClaims(refreshToken);
        String jti = claims.getId();
        String email = claims.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

        // Reuse detection: a denylisted jti means this token was already used.
        if (jti != null && authTokenRepository.existsByTypeAndToken(TokenType.REFRESH_REVOCATION, jti)) {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
            log.warn("Refresh token reuse detected for {} - all sessions revoked", email);
            throw ApiException.unauthorized("Refresh token reuse detected. All sessions have been revoked.");
        }

        Integer tokenVersion = claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class);
        if (tokenVersion == null || tokenVersion != user.getTokenVersion()) {
            throw ApiException.unauthorized("Refresh token is no longer valid");
        }

        revoke(jti, claims.getExpiration());
        return issueTokens(user);
    }

    /** Revokes a single refresh token (current-device logout). Idempotent. */
    @Transactional
    public void logout(String refreshToken) {
        try {
            Claims claims = jwtService.parseClaims(refreshToken);
            revoke(claims.getId(), claims.getExpiration());
        } catch (JwtException ex) {
            // Token already invalid/expired - nothing to revoke.
            log.debug("Logout called with an unparseable refresh token; ignoring");
        }
    }

    private Claims parseRefreshClaims(String refreshToken) {
        final Claims claims;
        try {
            claims = jwtService.parseClaims(refreshToken);
        } catch (JwtException ex) {
            throw ApiException.unauthorized("Invalid or expired refresh token");
        }
        if (!JwtService.TYPE_REFRESH.equals(claims.get(JwtService.CLAIM_TYPE, String.class))) {
            throw ApiException.unauthorized("Not a refresh token");
        }
        return claims;
    }

    private void revoke(String jti, Date expiration) {
        if (jti == null || authTokenRepository.existsByTypeAndToken(TokenType.REFRESH_REVOCATION, jti)) {
            return;
        }
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
        authTokenRepository.save(AuthToken.builder()
                .type(TokenType.REFRESH_REVOCATION)
                .token(jti)
                .expiresAt(expiresAt)
                .revokedAt(LocalDateTime.now())
                .build());
    }
}
