package com.billingplatformapplication.billingnovelties.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBillingNoveltyRequestDto(

        UUID workLogId,   // opcional — null si viene sin registro de horas

        @NotNull(message = "Developer ID is required")
        UUID developerId,

        @NotNull(message = "Client ID is required")
        UUID clientId,

        @NotBlank(message = "Novelty type is required")
        String noveltyType,

        @NotBlank(message = "Unit type is required")
        String unitType,

        @DecimalMin(value = "0.0", message = "Affected days cannot be negative")
        @Digits(integer = 2, fraction = 1)
        BigDecimal affectedDays,

        @DecimalMin(value = "0.0", message = "Affected hours cannot be negative")
        @Digits(integer = 3, fraction = 2)
        BigDecimal affectedHours,

        LocalDate startDate,
        LocalDate endDate,

        @DecimalMin(value = "0.0", message = "Manual discount cannot be negative")
        @Digits(integer = 15, fraction = 4)
        BigDecimal manualDiscountValue,

        @NotNull(message = "Billing year is required")
        @Min(2000) @Max(2100)
        Integer billingYear,

        @NotNull(message = "Billing month is required")
        @Min(1) @Max(12)
        Integer billingMonth,

        String observations
) {}