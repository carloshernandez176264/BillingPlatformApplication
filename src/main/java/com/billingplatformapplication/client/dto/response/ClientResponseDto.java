package com.billingplatformapplication.client.dto.response;


import java.time.Instant;
import java.util.UUID;

public record ClientResponseDto(
        UUID    id,
        String  taxId,
        String  companyName,
        String  tradeName,
        String  country,
        String  city,
        String  address,
        String  billingEmail,
        String  contactName,
        String  contactPhone,
        UUID    primaryCurrencyId,
        String  primaryCurrencyCode,
        String  taxRegime,
        String  notes,
        String  status,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String  createdBy,
        String  updatedBy
) {}

