package com.example.javaexam.repositories;

import com.example.javaexam.models.Bill;
import com.example.javaexam.models.enums.BillStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @EntityGraph(attributePaths = {"customer", "customer.profile", "meter", "reading", "tariffVersion"})
    Optional<Bill> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "customer.profile", "meter", "reading", "tariffVersion"})
    Optional<Bill> findByBillReference(String billReference);

    @EntityGraph(attributePaths = {"customer", "customer.profile", "meter", "reading", "tariffVersion"})
    List<Bill> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "customer.profile", "meter", "reading", "tariffVersion"})
    List<Bill> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "customer.profile", "meter", "reading", "tariffVersion"})
    List<Bill> findByCustomerIdAndStatusInOrderByCreatedAtDesc(Long customerId, List<BillStatus> statuses);

    boolean existsByReadingId(Long readingId);

    List<Bill> findByStatus(BillStatus status);
}
