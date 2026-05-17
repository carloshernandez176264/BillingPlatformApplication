package com.billingplatformapplication.billing.strategy;

import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HourlyRateStrategy implements RateCalculationStrategy {

    @Override
    public BigDecimal calculate(WorkLogEntity workLog, RateEntity rate) {
        return workLog.getActualWorkedHours()
                .multiply(rate.getHourlyRate())
                .setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public RateEntity.RateType getSupportedType() {
        return RateEntity.RateType.HOURLY;
    }
}
