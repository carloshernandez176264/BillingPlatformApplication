package com.billingplatformapplication.billingnovelties.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BillingNoveltyResponseDto(
        UUID id,
        UUID workLogId,
        UUID developerId,
        String developerName,
        UUID clientId,
        String clientName,
        String noveltyType,
        String unitType,
        BigDecimal affectedDays,
        BigDecimal affectedHours,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal calculatedDiscount,
        BigDecimal manualDiscountValue,
        BigDecimal effectiveDiscount,     // manual if present, otherwise calculated
        int billingYear,
        int billingMonth,
        String observations,
        String approvalStatus,
        String approvedBy,
        String rejectionReason,
        Instant createdAt,
        String createdBy
) {}


