package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.meter.CreateMeterReadingRequest;
import com.example.javaexam.dtos.meter.MeterReadingResponse;
import java.util.List;

public interface MeterReadingServiceContract {

    MeterReadingResponse capture(CreateMeterReadingRequest request);

    List<MeterReadingResponse> list();
}
