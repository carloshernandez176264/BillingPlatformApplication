package com.billingplatformapplication.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingCalculationResultDto {
    private UUID             clientId;
    private String           clientName;
    private int              billingYear;
    private int              billingMonth;
    private List<BillingLineDto> lines;
    private BigDecimal       subtotal;
    private BigDecimal       totalNoveltyDiscounts;
    private BigDecimal       totalOtherDiscounts;
    private BigDecimal       taxableAmount;
    private BigDecimal       taxAmount;
    private BigDecimal       totalAmount;

    public static BillingCalculationResultDto empty(UUID clientId, int year, int month) {
        return BillingCalculationResultDto.builder()
                .clientId(clientId).billingYear(year).billingMonth(month)
                .lines(List.of())
                .subtotal(BigDecimal.ZERO).totalNoveltyDiscounts(BigDecimal.ZERO)
                .totalOtherDiscounts(BigDecimal.ZERO).taxableAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO).totalAmount(BigDecimal.ZERO)
                .build();
    }
}


