package com.example.javaexam.security;

import com.example.javaexam.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import com.example.javaexam.exceptions.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final long accessExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLES, user.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toSet()))
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessExpirationMs))
                .signWith(signingKey)
                .compact();
    }

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

    public String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw ApiException.badRequest("Authorization header must contain a Bearer token");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
