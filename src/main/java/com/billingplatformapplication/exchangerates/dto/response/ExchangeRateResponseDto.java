package com.billingplatformapplication.exchangerates.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeRateResponseDto(
        UUID       id,
        UUID       fromCurrencyId,
        String     fromCurrencyCode,
        UUID       toCurrencyId,
        String     toCurrencyCode,
        BigDecimal rate,
        LocalDate  validFrom,
        LocalDate  validUntil,
        boolean    active
) {}
