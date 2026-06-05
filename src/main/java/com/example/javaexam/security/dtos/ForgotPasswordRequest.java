package com.example.javaexam.security.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for {@code POST /api/auth/forgot-password}. */
public record ForgotPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 255, message = "Email must be at most 255 characters")
        @Schema(example = "alice.mugisha@example.com")
        String email
) {}
