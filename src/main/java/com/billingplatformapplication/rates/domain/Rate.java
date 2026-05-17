package com.billingplatformapplication.rates.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain model — JPA-free.
 */
@Getter
@Builder
public class Rate {
    private UUID id;
    private UUID clientId;
    private UUID developerProfileId;
    private UUID currencyId;
    private String currencyCode;
    private String rateType;
    private BigDecimal monthlyRate;
    private BigDecimal dailyRate;
    private BigDecimal hourlyRate;
    private BigDecimal workingHoursPerDay;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private String status;
}

