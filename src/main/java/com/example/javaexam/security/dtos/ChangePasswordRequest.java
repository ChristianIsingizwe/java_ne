package com.example.javaexam.security.dtos;

import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload for {@code PUT /api/account/password}. */
public record ChangePasswordRequest(

        @NotBlank(message = "Current password is required")
        @Size(min = 8, max = 72, message = "Current password must be between 8 and 72 characters")
        @Schema(example = "ExamPass1!")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD,
                message = "New password must include uppercase, lowercase, number, special character, and no spaces")
        @Schema(example = "BetterPass2!")
        String newPassword
) {}
