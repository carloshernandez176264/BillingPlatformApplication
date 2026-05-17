package com.billingplatformapplication.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter @Builder
public class ClientAnnualReportDto {
    private UUID            clientId;
    private String          clientName;
    private int             year;
    private int             monthsWithInvoices;
    private BigDecimal      annualSubtotal;
    private BigDecimal      annualNoveltyDiscounts;
    private BigDecimal      annualTaxAmount;
    private BigDecimal      annualTotal;
    private BigDecimal      monthlyAverage;
    // Rentabilidad (si hay datos de salario)
    private BigDecimal      annualCost;
    private BigDecimal      annualMargin;
    private BigDecimal      marginPct;
    private List<ClientMonthDetailDto> months;
}