package com.billingplatformapplication.billing.strategy;

import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;

import java.math.BigDecimal;

public interface RateCalculationStrategy {
    BigDecimal calculate(WorkLogEntity workLog, RateEntity rate);
    RateEntity.RateType getSupportedType();
}








