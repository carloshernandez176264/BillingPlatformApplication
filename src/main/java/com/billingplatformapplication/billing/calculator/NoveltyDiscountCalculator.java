package com.billingplatformapplication.billing.calculator;

import com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.shared.util.MoneyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class NoveltyDiscountCalculator {

    /** Calcula el descuento de una sola novedad. Usado al guardar la novedad. */
    public BigDecimal calculateSingleDiscount(BillingNoveltyEntity novelty, RateEntity rate) {
        return calculateDiscount(novelty, resolveDailyRate(rate), resolveHourlyRate(rate));
    }

    /** Calcula el descuento total de una lista de novedades aprobadas. */
    public BigDecimal calculateTotalDiscount(List<BillingNoveltyEntity> novelties, RateEntity rate) {
        BigDecimal daily  = resolveDailyRate(rate);
        BigDecimal hourly = resolveHourlyRate(rate);
        return novelties.stream()
                .map(n -> calculateDiscount(n, daily, hourly))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
    }

    // ----------------------------------------------------------------

    private BigDecimal calculateDiscount(BillingNoveltyEntity novelty,
                                         BigDecimal dailyRate,
                                         BigDecimal hourlyRate) {
        // Valor manual siempre tiene prioridad
        if (MoneyUtils.isPositive(novelty.getManualDiscountValue())) {
            return novelty.getManualDiscountValue();
        }

        BigDecimal days  = MoneyUtils.safe(novelty.getAffectedDays());
        BigDecimal hours = MoneyUtils.safe(novelty.getAffectedHours());

        BigDecimal discount = switch (novelty.getUnitType()) {
            case DAYS  -> days.multiply(dailyRate);
            case HOURS -> hours.multiply(hourlyRate);
            case BOTH  -> days.multiply(dailyRate).add(hours.multiply(hourlyRate));
        };

        return discount.max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Resuelve el valor de un día según la tarifa.
     * Estándar colombiano: tarifa mensual / 21 días
     */
    private BigDecimal resolveDailyRate(RateEntity rate) {
        // Si tiene tarifa diaria explícita, la usa directamente
        if (MoneyUtils.isPositive(rate.getDailyRate())) {
            return rate.getDailyRate();
        }
        // Si tiene tarifa por hora y horas por día configuradas
        if (MoneyUtils.isPositive(rate.getHourlyRate())
                && MoneyUtils.isPositive(rate.getWorkingHoursPerDay())) {
            return rate.getHourlyRate()
                    .multiply(rate.getWorkingHoursPerDay())
                    .setScale(4, RoundingMode.HALF_UP);
        }
        // Estándar colombiano: tarifa mensual / 21 días laborales
        if (MoneyUtils.isPositive(rate.getMonthlyRate())) {
            return rate.getMonthlyRate()
                    .divide(new BigDecimal("21"), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Resuelve el valor de una hora según la tarifa.
     * Estándar colombiano: tarifa mensual / 168 horas
     */
    private BigDecimal resolveHourlyRate(RateEntity rate) {
        // Si tiene tarifa por hora explícita, la usa directamente
        if (MoneyUtils.isPositive(rate.getHourlyRate())) {
            return rate.getHourlyRate();
        }
        // Si tiene tarifa diaria y horas por día configuradas
        if (MoneyUtils.isPositive(rate.getDailyRate())
                && MoneyUtils.isPositive(rate.getWorkingHoursPerDay())) {
            return rate.getDailyRate()
                    .divide(rate.getWorkingHoursPerDay(), 4, RoundingMode.HALF_UP);
        }
        // Estándar colombiano: tarifa mensual / 168 horas laborales
        if (MoneyUtils.isPositive(rate.getMonthlyRate())) {
            return rate.getMonthlyRate()
                    .divide(new BigDecimal("168"), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}