package com.example.javaexam.security.dtos;

import com.example.javaexam.models.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

/** Token pair returned from login, refresh, and change-password. */
public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZS5tdWdpc2hhQGV4YW1wbGUuY29tIiwidHlwIjoiYWNjZXNzIn0.LK9kYlYtQm6x0QXwQHRx7f4FQ7p9h3C4Jq4g2v4xk8M")
        String accessToken,

        @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZS5tdWdpc2hhQGV4YW1wbGUuY29tIiwidHlwIjoicmVmcmVzaCJ9.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")
        String refreshToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(example = "900000")
        long accessExpiresInMs,

        @Schema(example = "604800000")
        long refreshExpiresInMs,

        @Schema(example = "alice.mugisha@example.com")
        String email,

        @Schema(example = "USER")
        Role role
) {}
