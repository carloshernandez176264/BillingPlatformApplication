package com.billingplatformapplication.shared.validation;

import com.billingplatformapplication.shared.exception.BusinessException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;

@UtilityClass
public class ValidationUtils {

    public static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException(field + " must be positive");
    }

    public static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException(field + " must be zero or positive");
    }

    public static void requireDateRange(LocalDate from, LocalDate until, String context) {
        if (from == null) throw new BusinessException(context + ": validFrom is required");
        if (until != null && until.isBefore(from))
            throw new BusinessException(context + ": validUntil must be after validFrom");
    }

    public static void requireNotBlank(String value, String field) {
        if (value == null || value.isBlank())
            throw new BusinessException(field + " must not be blank");
    }
}

