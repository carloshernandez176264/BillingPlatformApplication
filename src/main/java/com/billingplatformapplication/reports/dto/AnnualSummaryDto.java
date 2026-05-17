package com.billingplatformapplication.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AnnualSummaryDto {
    private int                   year;
    private int                   totalMonthsWithBilling;
    private BigDecimal            annualTotal;
    private BigDecimal            annualNoveltyDiscounts;
    private BigDecimal            annualTaxAmount;
    private List<MonthSummaryDto> months;
}