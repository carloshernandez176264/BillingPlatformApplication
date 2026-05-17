package com.billingplatformapplication.client.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record UpdateClientRequestDto(
        @Size(max = 200) String companyName,
        @Size(max = 200) String tradeName,
        @Size(max = 60)  String country,
        @Size(max = 100) String city,
        @Size(max = 300) String address,
        @Email @Size(max = 150) String billingEmail,
        @Size(max = 200) String contactName,
        @Size(max = 30)  String contactPhone,
        UUID   primaryCurrencyId,
        @Size(max = 100) String taxRegime,
        String notes,
        String status
) {}
