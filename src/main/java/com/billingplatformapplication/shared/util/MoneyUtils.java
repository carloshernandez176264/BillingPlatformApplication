package com.billingplatformapplication.shared.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class MoneyUtils {

    public static final int         SCALE = 4;
    public static final RoundingMode MODE  = RoundingMode.HALF_UP;

    public static BigDecimal round(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(SCALE, MODE);
    }

    public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
        if (base == null || pct == null) return BigDecimal.ZERO;
        return base.multiply(pct).divide(new BigDecimal("100"), SCALE, MODE);
    }

    public static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public static boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }
}

