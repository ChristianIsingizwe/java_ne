package com.example.javaexam.models;

import com.example.javaexam.models.enums.MeterBillingMode;
import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "meter_number", nullable = false, unique = true, length = 50)
    private String meterNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_mode", nullable = false, length = 20)
    private MeterBillingMode billingMode;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}
