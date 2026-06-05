package com.example.javaexam.repositories;

import com.example.javaexam.models.Meter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    @EntityGraph(attributePaths = {"customer", "customer.profile"})
    Optional<Meter> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "customer.profile"})
    Optional<Meter> findByMeterNumberIgnoreCase(String meterNumber);

    @EntityGraph(attributePaths = {"customer", "customer.profile"})
    List<Meter> findAllByOrderByIdAsc();

    boolean existsByMeterNumberIgnoreCase(String meterNumber);
}
