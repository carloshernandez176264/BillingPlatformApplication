package com.billingplatformapplication.exchangerates.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExchangeRateRequestDto(
        @NotNull UUID fromCurrencyId,
        @NotNull UUID toCurrencyId,
        @NotNull @DecimalMin("0.00000001") @Digits(integer = 11, fraction = 8) BigDecimal rate,
        @NotNull LocalDate validFrom,
        LocalDate validUntil
) {}
