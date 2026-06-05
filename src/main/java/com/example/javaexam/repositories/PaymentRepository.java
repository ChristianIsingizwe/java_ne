package com.example.javaexam.repositories;

import com.example.javaexam.models.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"bill", "bill.customer", "bill.customer.profile"})
    Optional<Payment> findById(Long id);

    @EntityGraph(attributePaths = {"bill", "bill.customer", "bill.customer.profile"})
    List<Payment> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"bill", "bill.customer", "bill.customer.profile"})
    List<Payment> findByBillCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"bill", "bill.customer", "bill.customer.profile"})
    List<Payment> findByBillCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, com.example.javaexam.models.enums.PaymentStatus status);

    @Query("select coalesce(sum(p.amountPaid), 0) from Payment p where p.bill.id = :billId and p.status = com.example.javaexam.models.enums.PaymentStatus.APPROVED and p.id <> :paymentId")
    BigDecimal sumApprovedAmountForBillExcludingPayment(@Param("billId") Long billId, @Param("paymentId") Long paymentId);
}
