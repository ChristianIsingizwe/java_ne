package com.example.javaexam.security.dtos;

import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload for {@code POST /api/auth/reset-password}. */
public record ResetPasswordRequest(

        @NotBlank(message = "Token is required")
        @Pattern(regexp = ValidationPatterns.UUID, message = "Token must be a valid UUID")
        @Schema(example = "5c0f0d7e-2f7d-4f91-9d73-8e9b7d2a66f1")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD,
                message = "Password must include uppercase, lowercase, number, special character, and no spaces")
        @Schema(example = "ResetPass3!")
        String newPassword
) {}
