package com.example.javaexam.security.dtos;

import com.example.javaexam.utils.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/** Payload for {@code POST /api/auth/register}. */
public record RegisterRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @Pattern(regexp = ValidationPatterns.HUMAN_NAME,
                message = "First name must contain letters only, with optional spaces, apostrophes, or hyphens")
        @Schema(example = "Alice")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @Pattern(regexp = ValidationPatterns.HUMAN_NAME,
                message = "Last name must contain letters only, with optional spaces, apostrophes, or hyphens")
        @Schema(example = "Mugisha")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 255, message = "Email must be at most 255 characters")
        @Schema(example = "alice.mugisha@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD,
                message = "Password must include uppercase, lowercase, number, special character, and no spaces")
        @Schema(example = "ExamPass1!")
        String password
) {}
