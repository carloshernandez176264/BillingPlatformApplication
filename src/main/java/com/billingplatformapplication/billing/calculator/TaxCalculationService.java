package com.billingplatformapplication.billing.calculator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxCalculationService {

    @Value("${billing.default-tax-percentage:0}")
    private BigDecimal defaultTaxPercentage;

    /** Calculates tax on a taxable base using the configured default percentage. */
    public BigDecimal calculate(BigDecimal taxableAmount) {
        return calculateWithPercentage(taxableAmount, defaultTaxPercentage);
    }

    public BigDecimal calculateWithPercentage(BigDecimal taxableAmount, BigDecimal percentage) {
        if (taxableAmount == null
                || percentage == null
                || percentage.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return taxableAmount
                .multiply(percentage)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }
}
