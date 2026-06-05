package com.example.javaexam.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_version_id", nullable = false)
    private TariffVersion tariffVersion;

    @Column(name = "from_unit", nullable = false)
    private Integer fromUnit;

    @Column(name = "to_unit")
    private Integer toUnit;

    @Column(name = "price_per_unit", nullable = false, precision = 14, scale = 2)
    private BigDecimal pricePerUnit;
}
