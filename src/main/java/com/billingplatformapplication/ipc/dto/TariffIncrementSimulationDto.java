package com.billingplatformapplication.ipc.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TariffIncrementSimulationDto(
        UUID       clientId,
        String     clientName,
        int        applyYear,
        BigDecimal ipcPercentage,
        List<DeveloperIncrementLineDto> lines,
        BigDecimal totalCurrentMonthly,
        BigDecimal totalNewMonthly,
        BigDecimal totalIncrement
) {}