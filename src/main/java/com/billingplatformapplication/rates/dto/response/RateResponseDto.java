package com.billingplatformapplication.rates.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RateResponseDto(
        UUID       id,
        UUID       clientId,
        String     clientName,
        UUID       developerProfileId,
        String     developerProfileName,
        UUID       currencyId,
        String     currencyCode,
        String     rateType,
        BigDecimal monthlyRate,
        BigDecimal dailyRate,
        BigDecimal hourlyRate,
        LocalDate  validFrom,
        LocalDate  validUntil,
        boolean    includesTax,
        BigDecimal taxPercentage,
        BigDecimal discountPercentage,
        BigDecimal workingHoursPerDay,
        String     status,
        String     commercialNotes,
        boolean    active,
        Instant    createdAt,
        String     createdBy
) {}

