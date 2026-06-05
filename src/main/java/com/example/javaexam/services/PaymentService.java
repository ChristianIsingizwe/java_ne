package com.example.javaexam.services;

import com.example.javaexam.dtos.payment.PaymentResponse;
import com.example.javaexam.dtos.payment.RecordPaymentRequest;
import com.example.javaexam.events.PaymentApprovedEvent;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Bill;
import com.example.javaexam.models.Payment;
import com.example.javaexam.models.User;
import com.example.javaexam.models.enums.BillStatus;
import com.example.javaexam.models.enums.PaymentStatus;
import com.example.javaexam.repositories.BillRepository;
import com.example.javaexam.repositories.PaymentRepository;
import com.example.javaexam.repositories.UserRepository;
import com.example.javaexam.services.contract.PaymentServiceContract;
import com.example.javaexam.utils.InputSanitizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentServiceContract {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public PaymentResponse record(RecordPaymentRequest request, String recordedByEmail) {
        String billReference = InputSanitizer.normalizeRequired(request.billReference(), "Bill reference").toUpperCase();
        Bill bill = billRepository.findByBillReference(billReference)
                .orElseThrow(() -> ApiException.notFound("Bill not found"));

        if (bill.getStatus() != BillStatus.APPROVED && bill.getStatus() != BillStatus.PARTIALLY_PAID) {
            throw ApiException.badRequest("Payments can only be recorded against approved or partially paid bills");
        }
        if (request.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw ApiException.badRequest("Payment amount cannot exceed the outstanding balance");
        }

        User recordedBy = userRepository.findByEmailIgnoreCase(recordedByEmail)
                .orElseThrow(() -> ApiException.notFound("Recording user not found"));
        Payment payment = paymentRepository.save(Payment.builder()
                .paymentReference("PAY-%s-%d".formatted(
                        bill.getBillReference().replaceAll("[^A-Za-z0-9]", ""),
                        System.currentTimeMillis()))
                .bill(bill)
                .amountPaid(request.amountPaid().setScale(2, RoundingMode.HALF_UP))
                .paymentMethod(request.paymentMethod())
                .paymentDate(request.paymentDate())
                .status(PaymentStatus.PENDING_APPROVAL)
                .recordedBy(recordedBy)
                .build());
        return applicationMapper.toPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse approve(Long paymentId, String approverEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("Payment not found"));
        if (payment.getStatus() != PaymentStatus.PENDING_APPROVAL) {
            throw ApiException.badRequest("Only pending payments can be approved");
        }
        BigDecimal alreadyApproved = paymentRepository.sumApprovedAmountForBillExcludingPayment(
                payment.getBill().getId(), payment.getId());
        if (alreadyApproved.add(payment.getAmountPaid()).compareTo(payment.getBill().getTotalAmount()) > 0) {
            throw ApiException.badRequest("Approving this payment would exceed the bill total");
        }

        User approver = userRepository.findByEmailIgnoreCase(approverEmail)
                .orElseThrow(() -> ApiException.notFound("Approving user not found"));
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setApprovedBy(approver);
        payment.setApprovedAt(LocalDateTime.now());

        // Bill settlement is finalized by a database trigger so partial and full payment updates
        // stay consistent regardless of how the payment row is approved.
        paymentRepository.save(payment);
        applicationEventPublisher.publishEvent(new PaymentApprovedEvent(payment.getId()));

        return applicationMapper.toPaymentResponse(payment);
    }

    public List<PaymentResponse> list() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(applicationMapper::toPaymentResponse)
                .toList();
    }

    public List<PaymentResponse> listForCustomer(Long customerId) {
        return paymentRepository.findByBillCustomerIdAndStatusOrderByCreatedAtDesc(customerId, PaymentStatus.APPROVED).stream()
                .map(applicationMapper::toPaymentResponse)
                .toList();
    }
}
