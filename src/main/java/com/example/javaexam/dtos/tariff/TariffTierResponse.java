package com.example.javaexam.dtos.tariff;

import java.math.BigDecimal;

public record TariffTierResponse(
        Integer fromUnit,
        Integer toUnit,
        BigDecimal pricePerUnit
) {
}
