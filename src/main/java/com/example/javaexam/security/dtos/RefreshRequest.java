package com.example.javaexam.security.dtos;

import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload for {@code POST /api/auth/refresh}. */
public record RefreshRequest(

        @NotBlank(message = "Refresh token is required")
        @Size(max = 2048, message = "Refresh token is too long")
        @Pattern(regexp = ValidationPatterns.JWT, message = "Refresh token must be a valid JWT")
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZS5tdWdpc2hhQGV4YW1wbGUuY29tIiwidHlwIjoicmVmcmVzaCJ9.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")
        String refreshToken
) {}
