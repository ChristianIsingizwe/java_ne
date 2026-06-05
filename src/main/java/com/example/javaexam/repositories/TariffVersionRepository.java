package com.example.javaexam.repositories;

import com.example.javaexam.models.TariffVersion;
import com.example.javaexam.models.enums.MeterType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffVersionRepository extends JpaRepository<TariffVersion, Long> {

    long countByMeterType(MeterType meterType);

    @EntityGraph(attributePaths = "tiers")
    List<TariffVersion> findAllByOrderByMeterTypeAscEffectiveFromDescVersionNumberDesc();

    @EntityGraph(attributePaths = "tiers")
    Optional<TariffVersion> findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionNumberDesc(
            MeterType meterType, LocalDate effectiveFrom);
}
