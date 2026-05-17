package com.billingplatformapplication.preinvoices.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record GeneratePreInvoiceRequestDto(
        @NotNull(message = "Client ID is required")    UUID    clientId,
        @NotNull(message = "Currency ID is required")  UUID    currencyId,
        @NotNull @Min(2000) @Max(2100)                 Integer billingYear,
        @NotNull @Min(1)    @Max(12)                   Integer billingMonth,
        String observations
) {}
