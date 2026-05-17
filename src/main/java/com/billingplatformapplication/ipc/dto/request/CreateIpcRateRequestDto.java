package com.billingplatformapplication.ipc.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateIpcRateRequestDto(
        @NotNull @Min(2000) @Max(2100)
        Integer year,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal ipcPercentage,

        String description,
        String source
) {}