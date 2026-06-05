package com.example.javaexam.dtos.tariff;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TariffTierRequest(
        @NotNull @Min(0) Integer fromUnit,
        Integer toUnit,
        @NotNull @DecimalMin("0.00") BigDecimal pricePerUnit
) {
}
