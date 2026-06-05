package com.example.javaexam.dtos.billing;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record GenerateBillRequest(
        @NotNull @Positive Long readingId,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-07-15", type = "string", format = "date")
        LocalDate dueDate
) {
}
