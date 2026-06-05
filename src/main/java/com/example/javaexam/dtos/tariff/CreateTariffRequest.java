package com.example.javaexam.dtos.tariff;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.TariffType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateTariffRequest(
        @NotNull MeterType meterType,
        @NotNull TariffType tariffType,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-07-01", type = "string", format = "date")
        LocalDate effectiveFrom,
        @NotNull @DecimalMin("0.00") BigDecimal fixedServiceCharge,
        @NotNull @DecimalMin("0.1800") @DecimalMax("0.1800") BigDecimal vatRate,
        @NotNull @DecimalMin("0.00") BigDecimal latePaymentPenaltyRate,
        @NotEmpty List<@Valid TariffTierRequest> tiers
) {
}
