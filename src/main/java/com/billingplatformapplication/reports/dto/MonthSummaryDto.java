package com.billingplatformapplication.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthSummaryDto {
    private int        month;
    private String     monthName;
    private int        clientCount;
    private int        developerCount;
    private BigDecimal subtotal;
    private BigDecimal noveltyDiscounts;
    private BigDecimal taxAmount;
    private BigDecimal total;
}