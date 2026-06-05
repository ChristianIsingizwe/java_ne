package com.example.javaexam.services;

import com.example.javaexam.dtos.tariff.CreateTariffRequest;
import com.example.javaexam.dtos.tariff.TariffTierRequest;
import com.example.javaexam.dtos.tariff.TariffVersionResponse;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.TariffTier;
import com.example.javaexam.models.TariffVersion;
import com.example.javaexam.models.enums.TariffType;
import com.example.javaexam.repositories.TariffVersionRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffVersionRepository tariffVersionRepository;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public TariffVersionResponse create(CreateTariffRequest request) {
        validateEffectiveDate(request);
        validateTiers(request.tariffType(), request.tiers());

        TariffVersion tariffVersion = TariffVersion.builder()
                .meterType(request.meterType())
                .tariffType(request.tariffType())
                .versionNumber((int) tariffVersionRepository.countByMeterType(request.meterType()) + 1)
                .effectiveFrom(request.effectiveFrom())
                .fixedServiceCharge(request.fixedServiceCharge())
                .vatRate(request.vatRate())
                .latePaymentPenaltyRate(request.latePaymentPenaltyRate())
                .build();

        List<TariffTier> tiers = request.tiers().stream()
                .sorted(Comparator.comparing(TariffTierRequest::fromUnit))
                .map(tier -> TariffTier.builder()
                        .tariffVersion(tariffVersion)
                        .fromUnit(tier.fromUnit())
                        .toUnit(tier.toUnit())
                        .pricePerUnit(tier.pricePerUnit())
                        .build())
                .toList();
        tariffVersion.setTiers(new java.util.ArrayList<>(tiers));

        return applicationMapper.toTariffVersionResponse(tariffVersionRepository.save(tariffVersion));
    }

    public List<TariffVersionResponse> list() {
        return tariffVersionRepository.findAllByOrderByMeterTypeAscEffectiveFromDescVersionNumberDesc().stream()
                .map(applicationMapper::toTariffVersionResponse)
                .toList();
    }

    private void validateEffectiveDate(CreateTariffRequest request) {
        boolean existingTariffs = tariffVersionRepository.countByMeterType(request.meterType()) > 0;
        if (existingTariffs && !request.effectiveFrom().isAfter(LocalDate.now().withDayOfMonth(1))) {
            throw ApiException.badRequest("New tariff versions must target a future billing cycle");
        }
    }

    private void validateTiers(TariffType tariffType, List<TariffTierRequest> tiers) {
        List<TariffTierRequest> sorted = tiers.stream()
                .sorted(Comparator.comparing(TariffTierRequest::fromUnit))
                .toList();

        if (tariffType == TariffType.FLAT) {
            if (sorted.size() != 1 || sorted.get(0).fromUnit() != 0 || sorted.get(0).toUnit() != null) {
                throw ApiException.badRequest("Flat tariffs require a single open-ended tier starting at 0");
            }
            return;
        }

        if (sorted.get(0).fromUnit() != 0) {
            throw ApiException.badRequest("Tiered tariffs must start at 0 units");
        }
        for (int index = 0; index < sorted.size(); index++) {
            TariffTierRequest tier = sorted.get(index);
            if (tier.toUnit() == null && index != sorted.size() - 1) {
                throw ApiException.badRequest("Only the last tier may be open-ended");
            }
            if (tier.toUnit() != null && tier.toUnit() <= tier.fromUnit()) {
                throw ApiException.badRequest("Tier upper bound must be greater than the lower bound");
            }
            if (index < sorted.size() - 1 && !sorted.get(index + 1).fromUnit().equals(tier.toUnit())) {
                throw ApiException.badRequest("Tier ranges must be contiguous");
            }
        }
    }
}
