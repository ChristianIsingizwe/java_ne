package com.example.javaexam.dtos.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.javaexam.models.enums.PaymentMethod;
import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        @NotBlank
        @Pattern(regexp = ValidationPatterns.BILL_REFERENCE, message = "Bill reference format is invalid")
        String billReference,
        @NotNull @DecimalMin("0.01") BigDecimal amountPaid,
        @NotNull PaymentMethod paymentMethod,
        @NotNull
        @PastOrPresent(message = "Payment date cannot be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-06-05", type = "string", format = "date")
        LocalDate paymentDate
) {
}
