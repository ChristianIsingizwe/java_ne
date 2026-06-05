package com.example.javaexam.dtos.customer;

import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Schema(example = "Jean Claude Uwimana")
        @NotBlank @Size(min = 3, max = 150) String fullName,
        @Schema(example = "1199081234567890")
        @NotBlank
        @Pattern(regexp = ValidationPatterns.NATIONAL_ID, message = "National ID must contain exactly 16 digits")
        String nationalId,
        @Schema(example = "jean.uwimana@example.com")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "+250788123456", description = "Rwanda phone number with optional country code")
        @NotBlank
        @Pattern(regexp = ValidationPatterns.PHONE, message = "Phone number must be a valid Rwanda number with optional country code +250")
        String phoneNumber,
        @Schema(example = "Kigali, Gasabo, Remera")
        @NotBlank @Size(min = 3, max = 255) String address,
        @Schema(example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        @NotNull RecordStatus status
) {
}
