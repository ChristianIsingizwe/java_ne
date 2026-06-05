package com.example.javaexam.models;

import com.example.javaexam.models.enums.BillStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill extends BaseEntity {

    @Column(name = "bill_reference", nullable = false, unique = true, length = 80)
    private String billReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reading_id", nullable = false, unique = true)
    private MeterReading reading;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_version_id", nullable = false)
    private TariffVersion tariffVersion;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "consumption_units", nullable = false, precision = 14, scale = 2)
    private BigDecimal consumptionUnits;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "vat_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "penalty_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "outstanding_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal outstandingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
