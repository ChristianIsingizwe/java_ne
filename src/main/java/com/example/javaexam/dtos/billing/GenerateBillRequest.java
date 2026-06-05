package com.example.javaexam.dtos.billing;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;

public record GenerateBillRequest(
        @NotNull Long readingId,
        @NotNull
        @Future(message = "Due date must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-07-15", type = "string", format = "date")
        LocalDate dueDate
) {
}
