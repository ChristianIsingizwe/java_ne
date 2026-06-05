package com.example.javaexam.dtos.meter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.javaexam.models.enums.MeterBillingMode;
import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record CreateMeterRequest(
        @NotNull Long customerId,
        @NotBlank @Pattern(regexp = ValidationPatterns.METER_NUMBER, message = "Meter number may only contain letters, digits, hyphens, or slashes") @Size(max = 50) String meterNumber,
        @NotNull MeterType meterType,
        @NotNull MeterBillingMode billingMode,
        @NotNull
        @PastOrPresent(message = "Installation date cannot be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-06-05", type = "string", format = "date")
        LocalDate installationDate,
        @NotNull RecordStatus status
) {
}
