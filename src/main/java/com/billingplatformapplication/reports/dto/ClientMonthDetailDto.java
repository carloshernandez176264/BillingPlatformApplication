package com.billingplatformapplication.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class ClientMonthDetailDto {
    private int        month;
    private String     monthName;
    private String     invoiceNumber;
    private BigDecimal subtotal;
    private BigDecimal noveltyDiscounts;
    private BigDecimal taxAmount;
    private BigDecimal total;
}