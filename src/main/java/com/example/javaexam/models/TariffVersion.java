package com.example.javaexam.models;

import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.TariffType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tariff_versions",
        uniqueConstraints = @UniqueConstraint(name = "uq_tariff_version_meter_effective", columnNames = {"meter_type", "effective_from", "version_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffVersion extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type", nullable = false, length = 20)
    private TariffType tariffType;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "fixed_service_charge", nullable = false, precision = 14, scale = 2)
    private BigDecimal fixedServiceCharge;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal vatRate;

    @Column(name = "late_payment_penalty_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal latePaymentPenaltyRate;

    @OneToMany(mappedBy = "tariffVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();
}
