package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.meter.CreateMeterRequest;
import com.example.javaexam.dtos.meter.CreateMeterReadingRequest;
import com.example.javaexam.dtos.meter.MeterReadingResponse;
import com.example.javaexam.dtos.meter.MeterResponse;
import com.example.javaexam.dtos.meter.UpdateMeterRequest;
import com.example.javaexam.services.contract.MeterReadingServiceContract;
import com.example.javaexam.services.contract.MeterServiceContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meters")
@SecuredApiErrorResponses
@Validated
public class MeterController {

    private final MeterServiceContract meterService;
    private final MeterReadingServiceContract meterReadingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "List meters")
    public List<MeterResponse> list() {
        return meterService.list();
    }

    @GetMapping("/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "Get a meter by id")
    public MeterResponse get(@PathVariable @Positive Long meterId) {
        return meterService.get(meterId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a utility meter")
    public MeterResponse create(@Valid @RequestBody CreateMeterRequest request) {
        return meterService.create(request);
    }

    @PutMapping("/{meterId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a utility meter")
    public MeterResponse update(@PathVariable @Positive Long meterId, @Valid @RequestBody UpdateMeterRequest request) {
        return meterService.update(meterId, request);
    }

    @PostMapping("/readings")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Capture a meter reading")
    public MeterReadingResponse captureReading(@Valid @RequestBody CreateMeterReadingRequest request) {
        return meterReadingService.capture(request);
    }

    @GetMapping("/readings/all")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "List captured meter readings")
    public List<MeterReadingResponse> readings() {
        return meterReadingService.list();
    }
}
