package com.example.javaexam.dtos.user;

import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.models.enums.RoleName;
import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateUserRequest(
        @Schema(example = "Alice Mukamana")
        @NotBlank @Size(min = 3, max = 150) String fullName,
        @Schema(example = "alice.mukamana@wasac.rw")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "0788123456", description = "Rwanda phone number in local format")
        @NotBlank
        @Pattern(regexp = ValidationPatterns.PHONE, message = "Phone number must be a valid Rwanda number in the format 07XXXXXXXX")
        String phoneNumber,
        @Schema(example = "1199081234567890", description = "Optional for staff users")
        @Pattern(regexp = "(^$)|" + ValidationPatterns.NATIONAL_ID, message = "National ID must be empty or contain exactly 16 digits")
        String nationalId,
        @Schema(example = "Kigali, Nyarugenge")
        @Size(max = 255) String address,
        @Schema(example = "SecurePass123")
        @NotBlank @Pattern(regexp = ValidationPatterns.PASSWORD, message = "Password must contain letters and digits and be 8 to 100 characters long") String password,
        @Schema(example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        @NotNull RecordStatus status,
        @Schema(example = "[\"ROLE_OPERATOR\"]", allowableValues = {"ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FINANCE", "ROLE_CUSTOMER"})
        @NotEmpty Set<RoleName> roles
) {
}
