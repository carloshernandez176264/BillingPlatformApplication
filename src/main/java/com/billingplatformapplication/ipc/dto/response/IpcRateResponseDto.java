package com.billingplatformapplication.ipc.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record IpcRateResponseDto(
        UUID       id,
        int        year,
        BigDecimal ipcPercentage,
        String     description,
        String     source,
        Instant    createdAt
) {}