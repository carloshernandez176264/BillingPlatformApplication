package com.billingplatformapplication.billing.service;

import com.billingplatformapplication.billing.calculator.NoveltyDiscountCalculator;
import com.billingplatformapplication.billing.calculator.TaxCalculationService;
import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.dto.BillingLineDto;
import com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity;
import com.billingplatformapplication.billingnovelties.repository.BillingNoveltyRepository;
import com.billingplatformapplication.client.entity.ClientDeveloperAssignmentEntity;
import com.billingplatformapplication.client.repository.ClientDeveloperAssignmentRepository;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.rates.service.RateService;
import com.billingplatformapplication.shared.util.MoneyUtils;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import com.billingplatformapplication.worklogs.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Motor de cálculo de facturación.
 *
 * Estándar colombiano:
 *   - 168 horas laborales / mes
 *   - 21 días laborales / mes
 *   - Valor día  = tarifa mensual / 21
 *   - Valor hora = tarifa mensual / 168
 *
 * El work_log es OPCIONAL:
 *   - Si existe → usa sus horas reales (permite ajuste por acuerdo con cliente)
 *   - Si no existe → usa la base estándar (168h / 21 días)
 *
 * Las novedades se descuentan siempre, con o sin work_log.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingCalculationService {

    // Estándar colombiano
    private static final BigDecimal HORAS_MES  = new BigDecimal("168");
    private static final BigDecimal DIAS_MES   = new BigDecimal("21");

    private final ClientDeveloperAssignmentRepository assignmentRepository;
    private final WorkLogRepository                   workLogRepository;
    private final BillingNoveltyRepository            noveltyRepository;
    private final RateService                         rateService;
    private final NoveltyDiscountCalculator           noveltyCalculator;
    private final TaxCalculationService               taxCalculationService;

    @Transactional(readOnly = true)
    public BillingCalculationResultDto calculateBilling(UUID clientId, int year, int month) {
        log.info("Cálculo de facturación — client={} period={}/{}", clientId, year, month);

        // Itera sobre desarrolladores ASIGNADOS al cliente (no sobre work_logs)
        List<ClientDeveloperAssignmentEntity> assignments =
                assignmentRepository.findActiveByClientWithDeveloper(clientId);

        if (assignments.isEmpty()) {
            log.warn("Sin desarrolladores asignados — client={} period={}/{}", clientId, year, month);
            return BillingCalculationResultDto.empty(clientId, year, month);
        }

        List<BillingLineDto> lines = new ArrayList<>();

        for (ClientDeveloperAssignmentEntity assignment : assignments) {
            UUID developerId = assignment.getDeveloper().getId();
            UUID profileId   = assignment.getDeveloper().getProfile() != null
                    ? assignment.getDeveloper().getProfile().getId() : null;

            if (profileId == null) {
                log.warn("Desarrollador sin perfil — developerId={}", developerId);
                continue;
            }

            // Buscar tarifa vigente
            Optional<RateEntity> rateOpt = rateService.findApplicableRate(
                    clientId, profileId, year, month);

            if (rateOpt.isEmpty()) {
                log.warn("Sin tarifa vigente — developer={} profile={} period={}/{}",
                        developerId, profileId, year, month);
                continue; // sin tarifa no se puede facturar
            }

            RateEntity rate = rateOpt.get();

            // Buscar work_log opcional
            Optional<WorkLogEntity> workLogOpt =
                    workLogRepository.findByClientDeveloperAndPeriod(
                            clientId, developerId, year, month);

            BillingLineDto line = calculateLine(
                    assignment, rate, workLogOpt.orElse(null), year, month);

            lines.add(line);
        }

        if (lines.isEmpty()) {
            log.warn("Sin líneas calculables — client={} period={}/{}", clientId, year, month);
            return BillingCalculationResultDto.empty(clientId, year, month);
        }

        return assembleResult(clientId, year, month, lines);
    }

    // ----------------------------------------------------------------

    private BillingLineDto calculateLine(ClientDeveloperAssignmentEntity assignment,
                                         RateEntity rate,
                                         WorkLogEntity workLog,
                                         int year, int month) {

        UUID developerId = assignment.getDeveloper().getId();
        UUID clientId    = assignment.getClient().getId();

        // Horas y días base — estándar colombiano o valor del work_log si existe
        BigDecimal horasBase;
        BigDecimal diasBase;

        if (workLog != null && workLog.getActualWorkedHours() != null) {
            // Usa las horas reales del work_log (acuerdo con cliente)
            horasBase = workLog.getActualWorkedHours();
            BigDecimal horasPerDia = rate.getWorkingHoursPerDay() != null
                    ? rate.getWorkingHoursPerDay() : new BigDecimal("8");
            diasBase = horasBase.divide(horasPerDia, 2, RoundingMode.HALF_UP);
            log.debug("Usando work_log id={} horas={}", workLog.getId(), horasBase);
        } else {
            // Estándar colombiano: 168h / 21 días
            horasBase = HORAS_MES;
            diasBase  = DIAS_MES;
            log.debug("Usando base estándar 168h/21d para developer={}", developerId);
        }

        // Monto bruto según tipo de tarifa
        BigDecimal grossAmount = calcularBruto(rate, horasBase, diasBase);

        // Novedades aprobadas del período para este developer+client
        List<BillingNoveltyEntity> novelties =
                noveltyRepository.findApprovedByDeveloperAndPeriod(
                        developerId, clientId, year, month);

        BigDecimal noveltyDiscount = noveltyCalculator.calculateTotalDiscount(novelties, rate);
        BigDecimal netAmount = grossAmount.subtract(noveltyDiscount).max(BigDecimal.ZERO);

        return BillingLineDto.builder()
                .workLogId(workLog != null ? workLog.getId() : null)
                .developerId(developerId)
                .developerName(assignment.getDeveloper().getFullName())
                .profileName(assignment.getDeveloper().getProfile().getName())
                .rateType(rate.getRateType().name())
                .rateValue(rateValue(rate))
                .billedHours(horasBase)
                .billedDays(diasBase)
                .grossAmount(MoneyUtils.round(grossAmount))
                .noveltyDiscount(MoneyUtils.round(noveltyDiscount))
                .otherDiscount(BigDecimal.ZERO)
                .netAmount(MoneyUtils.round(netAmount))
                .build();
    }

    /**
     * Calcula el monto bruto según el tipo de tarifa.
     * Para MONTHLY siempre es la tarifa completa (las novedades ajustan después).
     * Para DAILY/HOURLY multiplica por los días/horas base.
     */
    private BigDecimal calcularBruto(RateEntity rate,
                                     BigDecimal horasBase,
                                     BigDecimal diasBase) {
        return switch (rate.getRateType()) {
            case MONTHLY -> rate.getMonthlyRate();
            case DAILY   -> rate.getDailyRate()
                    .multiply(diasBase)
                    .setScale(4, RoundingMode.HALF_UP);
            case HOURLY  -> rate.getHourlyRate()
                    .multiply(horasBase)
                    .setScale(4, RoundingMode.HALF_UP);
        };
    }

    private BillingCalculationResultDto assembleResult(UUID clientId, int year, int month,
                                                       List<BillingLineDto> lines) {
        // Obtener nombre del cliente
        String clientName = assignmentRepository
                .findActiveByClientWithDeveloper(clientId)
                .stream().findFirst()
                .map(a -> a.getClient().getCompanyName())
                .orElse("");

        BigDecimal subtotal = lines.stream()
                .map(BillingLineDto::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNoveltyDiscounts = lines.stream()
                .map(BillingLineDto::getNoveltyDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxableAmount = subtotal.subtract(totalNoveltyDiscounts)
                .max(BigDecimal.ZERO);
        BigDecimal taxAmount   = taxCalculationService.calculate(taxableAmount);
        BigDecimal totalAmount = taxableAmount.add(taxAmount);

        return BillingCalculationResultDto.builder()
                .clientId(clientId)
                .clientName(clientName)
                .billingYear(year)
                .billingMonth(month)
                .lines(lines)
                .subtotal(MoneyUtils.round(subtotal))
                .totalNoveltyDiscounts(MoneyUtils.round(totalNoveltyDiscounts))
                .totalOtherDiscounts(BigDecimal.ZERO)
                .taxableAmount(MoneyUtils.round(taxableAmount))
                .taxAmount(MoneyUtils.round(taxAmount))
                .totalAmount(MoneyUtils.round(totalAmount))
                .build();
    }

    private BigDecimal rateValue(RateEntity rate) {
        return switch (rate.getRateType()) {
            case MONTHLY -> rate.getMonthlyRate();
            case DAILY   -> rate.getDailyRate();
            case HOURLY  -> rate.getHourlyRate();
        };
    }
}