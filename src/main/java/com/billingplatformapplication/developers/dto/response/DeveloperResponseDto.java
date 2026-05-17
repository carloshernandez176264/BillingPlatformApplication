package com.billingplatformapplication.developers.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeveloperResponseDto(
        UUID       id,
        String     documentId,
        String     documentType,
        String     fullName,
        String     email,
        UUID       profileId,
        String     profileName,
        LocalDate  hireDate,
        String     assignmentMode,
        String     status,
        boolean    active,
        Instant    createdAt,
        BigDecimal baseSalary  // null si el usuario no tiene permiso
) {}