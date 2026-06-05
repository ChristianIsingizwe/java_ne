package com.example.javaexam.dtos.billing;

import com.example.javaexam.models.enums.BillLineType;
import java.math.BigDecimal;

public record BillLineItemResponse(
        BillLineType lineType,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount
) {
}
