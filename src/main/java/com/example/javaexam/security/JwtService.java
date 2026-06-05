package com.example.javaexam.security;

import com.example.javaexam.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Creates and parses HMAC-signed JWTs.
 *
 * <p>Two token types are issued, distinguished by the {@code type} claim:
 * <ul>
 *   <li><b>access</b> — short-lived; carries {@code role} and {@code tv}
 *       (token version). Used to authenticate API requests.</li>
 *   <li><b>refresh</b> — long-lived; carries {@code tv} and a unique
 *       {@code jti} (JWT id). Exchanged at {@code /api/auth/refresh}.</li>
 * </ul>
 *
 * <p>{@link #parseClaims(String)} verifies the signature and expiry and throws
 * a {@link io.jsonwebtoken.JwtException} for any invalid token.
 */
@Service
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TOKEN_VERSION = "tv";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(User user) {
        return build(user, accessExpirationMs, Map.of(
                CLAIM_TYPE, TYPE_ACCESS,
                CLAIM_ROLE, user.getRole().name(),
                CLAIM_TOKEN_VERSION, user.getTokenVersion()), null);
    }

    public String generateRefreshToken(User user) {
        return build(user, refreshExpirationMs, Map.of(
                CLAIM_TYPE, TYPE_REFRESH,
                CLAIM_TOKEN_VERSION, user.getTokenVersion()), UUID.randomUUID().toString());
    }

    private String build(User user, long ttlMs, Map<String, Object> claims, String jti) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMs))
                .signWith(signingKey);
        if (jti != null) {
            builder.id(jti);
        }
        return builder.compact();
    }

    /** Verifies signature + expiry and returns the claims, or throws JwtException. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
