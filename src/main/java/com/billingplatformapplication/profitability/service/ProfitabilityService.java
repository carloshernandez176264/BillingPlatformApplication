package com.billingplatformapplication.profitability.service;

import com.billingplatformapplication.client.entity.ClientDeveloperAssignmentEntity;
import com.billingplatformapplication.client.repository.ClientDeveloperAssignmentRepository;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.profitability.dto.response.ProfitabilityResponseDto;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.rates.repository.RateRepository;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfitabilityService {

    // Cargas prestacionales Colombia (fijas por ley)
    private static final BigDecimal PCT_PRIMA              = new BigDecimal("0.0833");
    private static final BigDecimal PCT_CESANTIAS          = new BigDecimal("0.0833");
    private static final BigDecimal PCT_INT_CESANTIAS      = new BigDecimal("0.0100");
    private static final BigDecimal PCT_VACACIONES         = new BigDecimal("0.0417");
    private static final BigDecimal PCT_SALUD_EMPLEADOR    = new BigDecimal("0.0850");
    private static final BigDecimal PCT_PENSION_EMPLEADOR  = new BigDecimal("0.1200");
    private static final BigDecimal PCT_ARL                = new BigDecimal("0.0052");
    private static final BigDecimal PCT_CAJA               = new BigDecimal("0.0400");
    private static final BigDecimal PCT_ICBF               = new BigDecimal("0.0300");
    private static final BigDecimal PCT_SENA               = new BigDecimal("0.0200");

    private final ClientDeveloperAssignmentRepository assignmentRepository;
    private final ClientRepository                    clientRepository;
    private final RateRepository                      rateRepository;

    @Transactional(readOnly = true)
    public List<ProfitabilityResponseDto> calculateByClient(UUID clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<ClientDeveloperAssignmentEntity> assignments =
                assignmentRepository.findActiveByClientWithDeveloper(clientId);

        List<ProfitabilityResponseDto> results = new ArrayList<>();

        for (ClientDeveloperAssignmentEntity a : assignments) {
            var dev     = a.getDeveloper();
            var profile = dev.getProfile();
            if (profile == null || dev.getBaseSalary() == null) continue;

            // Buscar tarifa vigente del cliente
            var rateOpt = rateRepository
                    .findTopByClientIdAndDeveloperProfileIdAndStatusOrderByValidFromDesc(
                            clientId, profile.getId(), RateEntity.RateStatus.ACTIVE);
            if (rateOpt.isEmpty()) continue;

            RateEntity rate = rateOpt.get();
            BigDecimal clientRate = rate.getMonthlyRate() != null
                    ? rate.getMonthlyRate() : BigDecimal.ZERO;
            BigDecimal baseRate = profile.getBaseMonthlyRate() != null
                    ? profile.getBaseMonthlyRate() : BigDecimal.ZERO;
            BigDecimal salary = dev.getBaseSalary();

            // Calcular cargas prestacionales
            BigDecimal prima             = pct(salary, PCT_PRIMA);
            BigDecimal cesantias         = pct(salary, PCT_CESANTIAS);
            BigDecimal intCesantias      = pct(salary, PCT_INT_CESANTIAS);
            BigDecimal vacaciones        = pct(salary, PCT_VACACIONES);
            BigDecimal saludEmpleador    = pct(salary, PCT_SALUD_EMPLEADOR);
            BigDecimal pensionEmpleador  = pct(salary, PCT_PENSION_EMPLEADOR);
            BigDecimal arl               = pct(salary, PCT_ARL);
            BigDecimal caja              = pct(salary, PCT_CAJA);
            BigDecimal icbf              = pct(salary, PCT_ICBF);
            BigDecimal sena              = pct(salary, PCT_SENA);

            BigDecimal socialCharges = prima.add(cesantias).add(intCesantias)
                    .add(vacaciones).add(saludEmpleador).add(pensionEmpleador)
                    .add(arl).add(caja).add(icbf).add(sena);

            BigDecimal totalCost = salary.add(socialCharges);

            // Descuento vs tarifa base
            BigDecimal discountAmount = baseRate.subtract(clientRate).max(BigDecimal.ZERO);
            BigDecimal discountPct    = baseRate.compareTo(BigDecimal.ZERO) > 0
                    ? discountAmount.divide(baseRate, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;

            // Margen
            BigDecimal margin    = clientRate.subtract(totalCost);
            BigDecimal marginPct = clientRate.compareTo(BigDecimal.ZERO) > 0
                    ? margin.divide(clientRate, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;

            results.add(new ProfitabilityResponseDto(
                    dev.getId(), dev.getFullName(), profile.getName(),
                    a.getClient().getId(), a.getClient().getCompanyName(),
                    salary, round(socialCharges), round(totalCost),
                    round(prima), round(cesantias), round(intCesantias),
                    round(vacaciones), round(saludEmpleador), round(pensionEmpleador),
                    round(arl), round(caja), round(icbf), round(sena),
                    clientRate, baseRate,
                    round(discountAmount), round(discountPct),
                    round(margin), round(marginPct)
            ));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<ProfitabilityResponseDto> calculateAll() {
        // Todos los clientes
        return clientRepository.findAll().stream()
                .flatMap(c -> calculateByClient(c.getId()).stream())
                .toList();
    }

    private BigDecimal pct(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal round(BigDecimal val) {
        return val.setScale(0, RoundingMode.HALF_UP);
    }
}
