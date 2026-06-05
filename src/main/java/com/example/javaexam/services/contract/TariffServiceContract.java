package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.tariff.CreateTariffRequest;
import com.example.javaexam.dtos.tariff.TariffVersionResponse;
import java.util.List;

public interface TariffServiceContract {

    TariffVersionResponse create(CreateTariffRequest request);

    List<TariffVersionResponse> list();
}
