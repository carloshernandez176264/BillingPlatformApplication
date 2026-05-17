package com.billingplatformapplication.currencies.dto.request;



import jakarta.validation.constraints.*;

public record CreateCurrencyRequestDto(
        @NotBlank @Size(min = 3, max = 10)
        @Pattern(regexp = "^[A-Z]{3,10}$", message = "Code must be uppercase letters only")
        String code,

        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Size(max = 10)
        String symbol,

        @Min(0) @Max(8)
        int decimalPlaces
) {}

