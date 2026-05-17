package com.billingplatformapplication.worklogs.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkLogResponseDto(
        UUID       id,
        UUID       clientId,
        String     clientName,
        UUID       developerId,
        String     developerName,
        UUID       developerProfileId,
        String     developerProfileName,
        UUID       appliedRateId,
        String     appliedRateType,
        int        billingYear,
        int        billingMonth,
        int        expectedWorkingDays,
        BigDecimal expectedWorkingHours,
        BigDecimal actualWorkedHours,
        BigDecimal billableAmount,
        String     observations,
        String     status,
        Instant    createdAt,
        String     createdBy
) {}

