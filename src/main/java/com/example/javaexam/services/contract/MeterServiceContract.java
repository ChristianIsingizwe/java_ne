package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.meter.CreateMeterRequest;
import com.example.javaexam.dtos.meter.MeterResponse;
import com.example.javaexam.dtos.meter.UpdateMeterRequest;
import java.util.List;

public interface MeterServiceContract {

    MeterResponse create(CreateMeterRequest request);

    List<MeterResponse> list();

    MeterResponse get(Long meterId);

    MeterResponse update(Long meterId, UpdateMeterRequest request);
}
