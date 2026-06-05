package com.example.javaexam.services;

import com.example.javaexam.dtos.meter.CreateMeterReadingRequest;
import com.example.javaexam.dtos.meter.MeterReadingResponse;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Meter;
import com.example.javaexam.models.MeterReading;
import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.repositories.MeterReadingRepository;
import com.example.javaexam.repositories.MeterRepository;
import com.example.javaexam.services.contract.MeterReadingServiceContract;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeterReadingService implements MeterReadingServiceContract {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public MeterReadingResponse capture(CreateMeterReadingRequest request) {
        Meter meter = meterRepository.findById(request.meterId())
                .orElseThrow(() -> ApiException.notFound("Meter not found"));

        if (meter.getStatus() != RecordStatus.ACTIVE) {
            throw ApiException.badRequest("Meter must be active");
        }
        if (meter.getCustomer().getProfile().getStatus() != RecordStatus.ACTIVE) {
            throw ApiException.badRequest("Cannot capture readings for an inactive customer");
        }
        if (request.currentReading().compareTo(request.previousReading()) <= 0) {
            throw ApiException.badRequest("Current reading must be greater than previous reading");
        }

        int year = request.readingDate().getYear();
        int month = request.readingDate().getMonthValue();
        if (meterReadingRepository.existsByMeterIdAndReadingYearAndReadingMonth(meter.getId(), year, month)) {
            throw ApiException.conflict("Only one reading per meter is allowed in the same month");
        }

        MeterReading reading = meterReadingRepository.save(MeterReading.builder()
                .meter(meter)
                .previousReading(request.previousReading())
                .currentReading(request.currentReading())
                .readingDate(request.readingDate())
                .readingYear(year)
                .readingMonth(month)
                .build());
        return applicationMapper.toMeterReadingResponse(reading);
    }

    public List<MeterReadingResponse> list() {
        return meterReadingRepository.findAllByOrderByReadingYearDescReadingMonthDescIdDesc().stream()
                .map(applicationMapper::toMeterReadingResponse)
                .toList();
    }
}
