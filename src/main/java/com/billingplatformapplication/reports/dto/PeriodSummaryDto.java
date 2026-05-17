package com.billingplatformapplication.reports.dto;

import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PeriodSummaryDto {
    private int    billingYear;
    private int    billingMonth;
    private int    clientCount;
    private BigDecimal grandTotal;
    private List<BillingCalculationResultDto> clientResults;
}
