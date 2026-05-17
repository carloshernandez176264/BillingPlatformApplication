package com.billingplatformapplication.billing.usecase;

import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.service.BillingCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BillingCalculationUseCase {

    private final BillingCalculationService billingCalculationService;

    public BillingCalculationResultDto execute(UUID clientId, int year, int month) {
        return billingCalculationService.calculateBilling(clientId, year, month);
    }
}
