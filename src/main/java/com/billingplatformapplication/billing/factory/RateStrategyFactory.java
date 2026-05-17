package com.billingplatformapplication.billing.factory;

import com.billingplatformapplication.billing.strategy.RateCalculationStrategy;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RateStrategyFactory {

    private final Map<RateEntity.RateType, RateCalculationStrategy> strategies;

    public RateStrategyFactory(List<RateCalculationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        RateCalculationStrategy::getSupportedType,
                        Function.identity()
                ));
    }

    public RateCalculationStrategy getStrategy(RateEntity.RateType rateType) {
        RateCalculationStrategy strategy = strategies.get(rateType);
        if (strategy == null) {
            throw new BusinessException(
                    "No calculation strategy found for rate type: " + rateType);
        }
        return strategy;
    }
}
