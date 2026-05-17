package com.billingplatformapplication.developerprofiles.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DeveloperProfileResponseDto(
        UUID       id,
        String     name,
        String     level,
        String     description,
        String     baseSkills,
        BigDecimal baseMonthlyRate,  // tarifa base interna
        boolean    active,
        Instant    createdAt,
        String     createdBy
) {}
