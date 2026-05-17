package com.billingplatformapplication.ipc.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DeveloperIncrementLineDto(
        UUID       developerId,
        String     developerName,
        String     profileName,
        UUID       currentRateId,
        BigDecimal currentMonthlyRate,
        BigDecimal currentDailyRate,   // currentMonthlyRate / 21
        BigDecimal currentHourlyRate,  // currentMonthlyRate / 168
        BigDecimal newMonthlyRate,     // currentMonthlyRate * (1 + ipc/100)
        BigDecimal newDailyRate,       // newMonthlyRate / 21
        BigDecimal newHourlyRate,      // newMonthlyRate / 168
        BigDecimal increment           // diferencia mensual
) {}