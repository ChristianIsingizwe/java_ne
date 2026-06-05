package com.example.javaexam.services;

import com.example.javaexam.dtos.meter.CreateMeterRequest;
import com.example.javaexam.dtos.meter.MeterResponse;
import com.example.javaexam.dtos.meter.UpdateMeterRequest;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Customer;
import com.example.javaexam.models.Meter;
import com.example.javaexam.models.enums.MeterBillingMode;
import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.repositories.CustomerRepository;
import com.example.javaexam.repositories.MeterRepository;
import com.example.javaexam.utils.InputSanitizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public MeterResponse create(CreateMeterRequest request) {
        String meterNumber = InputSanitizer.normalizeRequired(request.meterNumber(), "Meter number").toUpperCase();
        if (meterRepository.existsByMeterNumberIgnoreCase(meterNumber)) {
            throw ApiException.conflict("Meter number already exists");
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> ApiException.notFound("Customer not found"));
        if (customer.getProfile().getStatus() != RecordStatus.ACTIVE) {
            throw ApiException.badRequest("Meters can only be assigned to active customers");
        }
        if (request.meterType() == MeterType.WATER && request.billingMode() == MeterBillingMode.PREPAID) {
            throw ApiException.badRequest("Water meters must use postpaid billing");
        }

        Meter meter = meterRepository.save(Meter.builder()
                .customer(customer)
                .meterNumber(meterNumber)
                .meterType(request.meterType())
                .billingMode(request.billingMode())
                .installationDate(request.installationDate())
                .status(request.status())
                .build());
        return applicationMapper.toMeterResponse(meter);
    }

    public List<MeterResponse> list() {
        return meterRepository.findAllByOrderByIdAsc().stream()
                .map(applicationMapper::toMeterResponse)
                .toList();
    }

    public MeterResponse get(Long meterId) {
        return applicationMapper.toMeterResponse(findMeter(meterId));
    }

    @Transactional
    public MeterResponse update(Long meterId, UpdateMeterRequest request) {
        Meter meter = findMeter(meterId);
        if (meter.getMeterType() == MeterType.WATER && request.billingMode() == MeterBillingMode.PREPAID) {
            throw ApiException.badRequest("Water meters must use postpaid billing");
        }
        meter.setBillingMode(request.billingMode());
        meter.setInstallationDate(request.installationDate());
        meter.setStatus(request.status());
        return applicationMapper.toMeterResponse(meterRepository.save(meter));
    }

    private Meter findMeter(Long meterId) {
        return meterRepository.findById(meterId)
                .orElseThrow(() -> ApiException.notFound("Meter not found"));
    }
}
