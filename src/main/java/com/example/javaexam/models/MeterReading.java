package com.example.javaexam.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "meter_readings",
        uniqueConstraints = @UniqueConstraint(name = "uq_meter_reading_period", columnNames = {"meter_id", "reading_year", "reading_month"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "previous_reading", nullable = false, precision = 14, scale = 2)
    private BigDecimal previousReading;

    @Column(name = "current_reading", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentReading;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "reading_year", nullable = false)
    private Integer readingYear;

    @Column(name = "reading_month", nullable = false)
    private Integer readingMonth;

    public BigDecimal getConsumption() {
        return currentReading.subtract(previousReading);
    }
}
