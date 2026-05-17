package com.billingplatformapplication.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingLineDto {
    private UUID workLogId;
    private UUID developerId;
    private String developerName;
    private String profileName;
    private String rateType;
    private BigDecimal rateValue;
    private BigDecimal billedHours;
    private BigDecimal billedDays;
    private BigDecimal grossAmount;
    private BigDecimal noveltyDiscount;
    private BigDecimal otherDiscount;
    private BigDecimal netAmount;
}
