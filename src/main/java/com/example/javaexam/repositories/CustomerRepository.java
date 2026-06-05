package com.example.javaexam.repositories;

import com.example.javaexam.models.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @EntityGraph(attributePaths = "profile")
    List<Customer> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "profile")
    Optional<Customer> findById(Long id);

    @EntityGraph(attributePaths = "profile")
    Optional<Customer> findByProfileEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "profile")
    Optional<Customer> findByProfileId(Long profileId);

    boolean existsByProfileNationalId(String nationalId);
}
