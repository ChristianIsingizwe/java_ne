package com.example.javaexam.services;

import com.example.javaexam.dtos.billing.BillResponse;
import com.example.javaexam.dtos.billing.GenerateBillRequest;
import com.example.javaexam.events.BillApprovedEvent;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Bill;
import com.example.javaexam.models.BillLineItem;
import com.example.javaexam.models.Meter;
import com.example.javaexam.models.MeterReading;
import com.example.javaexam.models.TariffTier;
import com.example.javaexam.models.TariffVersion;
import com.example.javaexam.models.User;
import com.example.javaexam.models.enums.BillLineType;
import com.example.javaexam.models.enums.BillStatus;
import com.example.javaexam.models.enums.MeterBillingMode;
import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.repositories.BillLineItemRepository;
import com.example.javaexam.repositories.BillRepository;
import com.example.javaexam.repositories.MeterReadingRepository;
import com.example.javaexam.repositories.TariffVersionRepository;
import com.example.javaexam.repositories.UserRepository;
import com.example.javaexam.services.contract.BillingServiceContract;
import com.example.javaexam.utils.InputSanitizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService implements BillingServiceContract {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final BillRepository billRepository;
    private final BillLineItemRepository billLineItemRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffVersionRepository tariffVersionRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationGenerationService notificationGenerationService;

    @Transactional
    public BillResponse generate(GenerateBillRequest request, String generatedByEmail) {
        MeterReading reading = meterReadingRepository.findById(request.readingId())
                .orElseThrow(() -> ApiException.notFound("Meter reading not found"));
        Meter meter = reading.getMeter();

        if (billRepository.existsByReadingId(reading.getId())) {
            throw ApiException.conflict("A bill has already been generated for this reading");
        }
        if (meter.getStatus() != RecordStatus.ACTIVE) {
            throw ApiException.badRequest("Inactive meters cannot be billed");
        }
        if (meter.getCustomer().getProfile().getStatus() != RecordStatus.ACTIVE) {
            throw ApiException.badRequest("Inactive customers cannot receive bills");
        }
        if (meter.getBillingMode() != MeterBillingMode.POSTPAID) {
            throw ApiException.badRequest("Only postpaid meters are supported");
        }
        if (!request.dueDate().isAfter(reading.getReadingDate())) {
            throw ApiException.badRequest("Bill due date must be after the reading date");
        }

        BigDecimal consumption = reading.getConsumption();
        if (consumption.compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Consumption must be greater than zero");
        }

        TariffVersion tariffVersion = tariffVersionRepository
                .findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionNumberDesc(
                        meter.getMeterType(), YearMonth.of(reading.getReadingYear(), reading.getReadingMonth()).atDay(1))
                .orElseThrow(() -> ApiException.unprocessable("No tariff configuration exists for this billing period"));

        BigDecimal consumptionCharge = calculateConsumptionCharge(consumption, tariffVersion);
        BigDecimal serviceCharge = scale(tariffVersion.getFixedServiceCharge());
        BigDecimal subtotal = scale(consumptionCharge.add(serviceCharge));
        BigDecimal vatAmount = scale(subtotal.multiply(tariffVersion.getVatRate()));
        BigDecimal totalAmount = scale(subtotal.add(vatAmount));
        User generatedBy = userRepository.findByEmailIgnoreCase(generatedByEmail)
                .orElseThrow(() -> ApiException.notFound("Generating user not found"));

        // Bills start pending so finance/admin can review generated amounts before the customer
        // sees them or any payment is recorded against them.
        Bill bill = billRepository.save(Bill.builder()
                .billReference(buildBillReference(reading))
                .customer(meter.getCustomer())
                .meter(meter)
                .reading(reading)
                .tariffVersion(tariffVersion)
                .billingYear(reading.getReadingYear())
                .billingMonth(reading.getReadingMonth())
                .dueDate(request.dueDate())
                .consumptionUnits(scale(consumption))
                .subtotal(subtotal)
                .vatAmount(vatAmount)
                .penaltyAmount(ZERO)
                .totalAmount(totalAmount)
                .amountPaid(ZERO)
                .outstandingBalance(totalAmount)
                .status(BillStatus.PENDING_APPROVAL)
                .generatedBy(generatedBy)
                .build());

        // Line items preserve the billing breakdown that was used to derive the aggregate totals.
        billLineItemRepository.saveAll(List.of(
                BillLineItem.builder()
                        .bill(bill)
                        .lineType(BillLineType.CONSUMPTION)
                        .description("Consumption charge")
                        .quantity(scale(consumption))
                        .unitPrice(resolveBaseUnitPrice(tariffVersion))
                        .amount(consumptionCharge)
                        .displayOrder(1)
                        .build(),
                BillLineItem.builder()
                        .bill(bill)
                        .lineType(BillLineType.SERVICE_CHARGE)
                        .description("Fixed service charge")
                        .quantity(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP))
                        .unitPrice(serviceCharge)
                        .amount(serviceCharge)
                        .displayOrder(2)
                        .build(),
                BillLineItem.builder()
                        .bill(bill)
                        .lineType(BillLineType.VAT)
                        .description("VAT")
                        .quantity(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP))
                        .unitPrice(vatAmount)
                        .amount(vatAmount)
                        .displayOrder(3)
                        .build()));

        return applicationMapper.toBillResponse(
                bill, billLineItemRepository.findByBillIdOrderByDisplayOrderAsc(bill.getId()));
    }

    @Transactional
    public BillResponse approve(Long billId, String approverEmail) {
        Bill bill = findBill(billId);
        if (bill.getStatus() != BillStatus.PENDING_APPROVAL) {
            throw ApiException.badRequest("Only pending bills can be approved");
        }

        User approver = userRepository.findByEmailIgnoreCase(approverEmail)
                .orElseThrow(() -> ApiException.notFound("Approving user not found"));
        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedBy(approver);
        bill.setApprovedAt(LocalDateTime.now());
        billRepository.save(bill);
        notificationGenerationService.createBillNotificationIfMissing(bill);
        applicationEventPublisher.publishEvent(new BillApprovedEvent(bill.getId()));

        return applicationMapper.toBillResponse(
                bill, billLineItemRepository.findByBillIdOrderByDisplayOrderAsc(bill.getId()));
    }

    @Transactional
    public BillResponse applyLatePenalty(Long billId) {
        Bill bill = findBill(billId);
        if (bill.getStatus() == BillStatus.PAID) {
            throw ApiException.badRequest("Paid bills cannot receive penalties");
        }
        if (!bill.getDueDate().isBefore(LocalDate.now())) {
            throw ApiException.badRequest("Late penalty can only be applied after the due date");
        }
        if (bill.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw ApiException.badRequest("Late penalty has already been applied");
        }

        BigDecimal penalty = scale(bill.getOutstandingBalance().multiply(bill.getTariffVersion().getLatePaymentPenaltyRate()));
        bill.setPenaltyAmount(penalty);
        bill.setTotalAmount(scale(bill.getTotalAmount().add(penalty)));
        bill.setOutstandingBalance(scale(bill.getOutstandingBalance().add(penalty)));
        billRepository.save(bill);

        billLineItemRepository.save(BillLineItem.builder()
                .bill(bill)
                .lineType(BillLineType.LATE_PENALTY)
                .description("Late payment penalty")
                .quantity(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP))
                .unitPrice(penalty)
                .amount(penalty)
                .displayOrder(4)
                .build());

        return applicationMapper.toBillResponse(
                bill, billLineItemRepository.findByBillIdOrderByDisplayOrderAsc(bill.getId()));
    }

    public List<BillResponse> list() {
        return billRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(bill -> applicationMapper.toBillResponse(
                        bill, billLineItemRepository.findByBillIdOrderByDisplayOrderAsc(bill.getId())))
                .toList();
    }

    public List<BillResponse> listForCustomer(Long customerId) {
        return billRepository.findByCustomerIdAndStatusInOrderByCreatedAtDesc(
                        customerId, List.of(BillStatus.APPROVED, BillStatus.PARTIALLY_PAID, BillStatus.PAID)).stream()
                .map(bill -> applicationMapper.toBillResponse(
                        bill, billLineItemRepository.findByBillIdOrderByDisplayOrderAsc(bill.getId())))
                .toList();
    }

    public BillResponse getByReference(String billReference) {
        String normalizedReference = InputSanitizer.normalizeRequired(billReference, "Bill reference").toUpperCase();
        Bill bill = billRepository.findByBillReference(normalizedReference)
                .orElseThrow(() -> ApiException.notFound("Bill not found"));
        return applicationMapper.toBillResponse(
                bill, billLineItemRepository.findByBillIdOrderByDisplayOrderAsc(bill.getId()));
    }

    private Bill findBill(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> ApiException.notFound("Bill not found"));
    }

    private BigDecimal calculateConsumptionCharge(BigDecimal consumption, TariffVersion tariffVersion) {
        BigDecimal charge = BigDecimal.ZERO;
        List<TariffTier> tiers = tariffVersion.getTiers().stream()
                .sorted(Comparator.comparing(TariffTier::getFromUnit))
                .toList();

        // Tier bounds are interpreted as progressive brackets rather than a single rate applied
        // to all consumed units.
        for (TariffTier tier : tiers) {
            BigDecimal lower = BigDecimal.valueOf(tier.getFromUnit());
            BigDecimal upper = tier.getToUnit() == null ? consumption : BigDecimal.valueOf(tier.getToUnit());
            if (consumption.compareTo(lower) <= 0) {
                continue;
            }
            BigDecimal tierUnits = consumption.min(upper).subtract(lower);
            if (tierUnits.compareTo(BigDecimal.ZERO) > 0) {
                charge = charge.add(tierUnits.multiply(tier.getPricePerUnit()));
            }
        }
        return scale(charge);
    }

    private String buildBillReference(MeterReading reading) {
        String safeMeter = reading.getMeter().getMeterNumber().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return "BILL-%04d%02d-%s".formatted(reading.getReadingYear(), reading.getReadingMonth(), safeMeter);
    }

    private BigDecimal resolveBaseUnitPrice(TariffVersion tariffVersion) {
        // The response exposes a representative unit price for display; the actual charge is
        // always taken from the tier-by-tier calculation above.
        return tariffVersion.getTiers().stream()
                .min(Comparator.comparing(TariffTier::getFromUnit))
                .map(TariffTier::getPricePerUnit)
                .map(this::scale)
                .orElse(ZERO);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
