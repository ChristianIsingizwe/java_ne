package com.example.javaexam.dtos.meter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMeterReadingRequest(
        @NotNull @Positive Long meterId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal previousReading,
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal currentReading,
        @NotNull
        @PastOrPresent(message = "Reading date cannot be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-06-05", type = "string", format = "date")
        LocalDate readingDate
) {
}
