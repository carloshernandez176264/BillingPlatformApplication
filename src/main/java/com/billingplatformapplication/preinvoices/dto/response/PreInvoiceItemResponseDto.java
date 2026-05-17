package com.billingplatformapplication.preinvoices.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PreInvoiceItemResponseDto(
        UUID       id,
        UUID       workLogId,
        UUID       developerId,
        String     developerName,
        UUID       developerProfileId,
        String     developerProfileName,
        String     rateType,
        BigDecimal rateValue,
        BigDecimal billedHours,
        BigDecimal billedDays,
        BigDecimal grossAmount,
        BigDecimal noveltyDiscount,
        BigDecimal otherDiscount,
        BigDecimal netAmount,
        String     lineDescription,
        int        sortOrder
) {}

