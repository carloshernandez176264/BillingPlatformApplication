package com.billingplatformapplication.billing.strategy;

import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MonthlyRateStrategy implements RateCalculationStrategy {

    @Override
    public BigDecimal calculate(WorkLogEntity workLog, RateEntity rate) {
        if (workLog.getExpectedWorkingHours() == null
                || workLog.getExpectedWorkingHours().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal workRatio = workLog.getActualWorkedHours()
                .divide(workLog.getExpectedWorkingHours(), 6, RoundingMode.HALF_UP);

        return rate.getMonthlyRate()
                .multiply(workRatio)
                .setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public RateEntity.RateType getSupportedType() {
        return RateEntity.RateType.MONTHLY;
    }
}
