package com.example.javaexam.repositories;

import com.example.javaexam.models.MeterReading;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    boolean existsByMeterIdAndReadingYearAndReadingMonth(Long meterId, Integer readingYear, Integer readingMonth);

    @EntityGraph(attributePaths = {"meter", "meter.customer", "meter.customer.profile"})
    Optional<MeterReading> findById(Long id);

    @EntityGraph(attributePaths = {"meter", "meter.customer", "meter.customer.profile"})
    Optional<MeterReading> findTopByMeterIdOrderByReadingYearDescReadingMonthDescReadingDateDescIdDesc(Long meterId);

    @EntityGraph(attributePaths = {"meter", "meter.customer", "meter.customer.profile"})
    List<MeterReading> findAllByOrderByReadingYearDescReadingMonthDescIdDesc();
}
