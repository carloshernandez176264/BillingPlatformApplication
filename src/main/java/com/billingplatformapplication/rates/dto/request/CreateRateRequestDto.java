package com.billingplatformapplication.rates.dto.request;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRateRequestDto(
        UUID clientId,   // null = base rate for all clients

        @NotNull(message = "Developer profile ID is required")
        UUID developerProfileId,

        @NotNull(message = "Currency ID is required")
        UUID currencyId,

        @NotBlank(message = "Rate type is required")
        String rateType,  // MONTHLY | DAILY | HOURLY

        @DecimalMin("0.0001") @Digits(integer = 15, fraction = 4)
        BigDecimal monthlyRate,

        @DecimalMin("0.0001") @Digits(integer = 15, fraction = 4)
        BigDecimal dailyRate,

        @DecimalMin("0.0001") @Digits(integer = 15, fraction = 4)
        BigDecimal hourlyRate,

        @NotNull(message = "Valid from date is required")
        LocalDate validFrom,

        LocalDate validUntil,

        boolean includesTax,

        @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2)
        BigDecimal taxPercentage,

        @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2)
        BigDecimal discountPercentage,

        @DecimalMin("0.5") @DecimalMax("24")
        BigDecimal workingHoursPerDay,

        String commercialNotes
) {}

