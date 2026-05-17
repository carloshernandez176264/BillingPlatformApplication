package com.billingplatformapplication.worklogs.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWorkLogRequestDto(
        @NotNull UUID clientId,
        @NotNull UUID developerId,
        @NotNull UUID developerProfileId,
        @NotNull @Min(2000) @Max(2100) Integer billingYear,
        @NotNull @Min(1)    @Max(12)   Integer billingMonth,
        @NotNull @Min(1)    @Max(31)   Integer expectedWorkingDays,

        @NotNull @DecimalMin("0.01") @Digits(integer = 4, fraction = 2)
        BigDecimal expectedWorkingHours,

        @NotNull @DecimalMin("0.0")  @Digits(integer = 4, fraction = 2)
        BigDecimal actualWorkedHours,

        String observations
) {}
