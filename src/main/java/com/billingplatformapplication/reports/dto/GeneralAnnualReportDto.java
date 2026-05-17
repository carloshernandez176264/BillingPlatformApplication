package com.billingplatformapplication.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Builder
public class GeneralAnnualReportDto {
    private int                      year;
    private int                      totalClients;
    private int                      totalInvoices;
    private BigDecimal               grandTotal;
    private BigDecimal               grandNoveltyDiscounts;
    private BigDecimal               grandTaxAmount;
    private List<ClientAnnualReportDto> clientSummaries;
    private List<MonthSummaryDto>    monthlyTotals;
}