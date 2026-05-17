package com.billingplatformapplication.client.dto.request;


import jakarta.validation.constraints.*;

import java.util.UUID;


public record CreateClientRequestDto(
        @NotBlank @Size(max = 30)                             String taxId,
        @NotBlank @Size(max = 200)                            String companyName,
        @Size(max = 200)                                      String tradeName,
        @NotBlank @Size(max = 60)                             String country,
        @Size(max = 100)                                      String city,
        @Size(max = 300)                                      String address,
        @NotBlank @Email @Size(max = 150)                     String billingEmail,
        @Size(max = 200)                                      String contactName,
        @Size(max = 30)                                       String contactPhone,
        @NotNull(message = "Primary currency ID is required") UUID primaryCurrencyId,
        @Size(max = 100)                                      String taxRegime,
        String notes
) {}
