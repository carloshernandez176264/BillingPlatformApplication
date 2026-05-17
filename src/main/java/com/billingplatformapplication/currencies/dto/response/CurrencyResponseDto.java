package com.billingplatformapplication.currencies.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CurrencyResponseDto(
        UUID    id,
        String  code,
        String  name,
        String  symbol,
        int     decimalPlaces,
        boolean active,
        Instant createdAt,
        String  createdBy
) {}