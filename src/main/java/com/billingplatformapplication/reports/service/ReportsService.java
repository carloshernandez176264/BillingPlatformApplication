package com.billingplatformapplication.reports.service;


import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.service.BillingCalculationService;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.reports.dto.PeriodSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final BillingCalculationService billingCalculationService;
    private final ClientRepository clientRepository;

    /**
     * Generates a billing summary across all active clients for a period.
     */
    @Transactional(readOnly = true)
    public PeriodSummaryDto periodSummary(int year, int month) {
        List<BillingCalculationResultDto> results = clientRepository.findByActiveTrue()
                .stream()
                .map(c -> billingCalculationService.calculateBilling(c.getId(), year, month))
                .filter(r -> !r.getLines().isEmpty())
                .collect(Collectors.toList());

        BigDecimal grandTotal = results.stream()
                .map(BillingCalculationResultDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PeriodSummaryDto.builder()
                .billingYear(year)
                .billingMonth(month)
                .clientCount(results.size())
                .grandTotal(grandTotal)
                .clientResults(results)
                .build();
    }
}

