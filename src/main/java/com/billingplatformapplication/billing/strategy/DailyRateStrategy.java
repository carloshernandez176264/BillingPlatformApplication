package com.billingplatformapplication.billing.strategy;

import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DailyRateStrategy implements RateCalculationStrategy {

    @Override
    public BigDecimal calculate(WorkLogEntity workLog, RateEntity rate) {
        BigDecimal hoursPerDay = rate.getWorkingHoursPerDay() != null
                ? rate.getWorkingHoursPerDay()
                : new BigDecimal("8.00");

        BigDecimal billedDays = workLog.getActualWorkedHours()
                .divide(hoursPerDay, 6, RoundingMode.HALF_UP);

        return billedDays.multiply(rate.getDailyRate())
                .setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public RateEntity.RateType getSupportedType() {
        return RateEntity.RateType.DAILY;
    }
}

