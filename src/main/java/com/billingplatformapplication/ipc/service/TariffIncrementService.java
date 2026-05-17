package com.billingplatformapplication.ipc.service;

import com.billingplatformapplication.client.entity.ClientDeveloperAssignmentEntity;
import com.billingplatformapplication.client.repository.ClientDeveloperAssignmentRepository;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.ipc.dto.*;
import com.billingplatformapplication.ipc.dto.request.ApproveIncrementRequestDto;
import com.billingplatformapplication.ipc.dto.request.CreateIpcRateRequestDto;
import com.billingplatformapplication.ipc.dto.response.IpcRateResponseDto;
import com.billingplatformapplication.ipc.entity.IpcRateEntity;
import com.billingplatformapplication.ipc.entity.TariffIncrementEntity;
import com.billingplatformapplication.ipc.repository.IpcRateRepository;
import com.billingplatformapplication.ipc.repository.TariffIncrementRepository;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.rates.repository.RateRepository;
import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TariffIncrementService {

    private static final BigDecimal DIAS_MES  = new BigDecimal("21");
    private static final BigDecimal HORAS_MES = new BigDecimal("168");

    private final IpcRateRepository                   ipcRateRepository;
    private final TariffIncrementRepository           incrementRepository;
    private final ClientDeveloperAssignmentRepository assignmentRepository;
    private final ClientRepository                    clientRepository;
    private final RateRepository                      rateRepository;

    // ---- IPC ----

    @Transactional(readOnly = true)
    public List<IpcRateResponseDto> findAllIpc() {
        return ipcRateRepository.findAllByOrderByYearDesc()
                .stream().map(this::toIpcDto).toList();
    }

    @Transactional
    public IpcRateResponseDto createIpc(CreateIpcRateRequestDto request) {
        if (ipcRateRepository.existsByYear(request.year())) {
            throw new BusinessException("Ya existe un IPC registrado para el año " + request.year());
        }
        IpcRateEntity entity = IpcRateEntity.builder()
                .year(request.year())
                .ipcPercentage(request.ipcPercentage())
                .description(request.description())
                .source(request.source() != null ? request.source() : "DANE - Colombia")
                .build();
        return toIpcDto(ipcRateRepository.save(entity));
    }

    // ---- Simulación ----

    @Transactional(readOnly = true)
    public TariffIncrementSimulationDto simulate(UUID clientId, UUID ipcRateId) {

        var client  = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
        var ipcRate = ipcRateRepository.findById(ipcRateId)
                .orElseThrow(() -> new ResourceNotFoundException("IpcRate", ipcRateId));

        int applyYear = ipcRate.getYear() + 1; // el IPC de 2024 aplica en 2025
        BigDecimal factor = BigDecimal.ONE.add(
                ipcRate.getIpcPercentage().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));

        List<ClientDeveloperAssignmentEntity> assignments =
                assignmentRepository.findActiveByClientWithDeveloper(clientId);

        if (assignments.isEmpty()) {
            throw new BusinessException(
                    "El cliente no tiene desarrolladores asignados");
        }

        List<DeveloperIncrementLineDto> lines = assignments.stream()
                .map(a -> buildLine(a, factor, applyYear))
                .filter(l -> l != null)
                .toList();

        if (lines.isEmpty()) {
            throw new BusinessException(
                    "No se encontraron tarifas vigentes para los desarrolladores del cliente");
        }

        BigDecimal totalCurrent = lines.stream()
                .map(DeveloperIncrementLineDto::currentMonthlyRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNew = lines.stream()
                .map(DeveloperIncrementLineDto::newMonthlyRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TariffIncrementSimulationDto(
                clientId, client.getCompanyName(),
                applyYear, ipcRate.getIpcPercentage(),
                lines, totalCurrent, totalNew,
                totalNew.subtract(totalCurrent)
        );
    }

    private DeveloperIncrementLineDto buildLine(ClientDeveloperAssignmentEntity assignment,
                                                BigDecimal factor, int applyYear) {
        var dev     = assignment.getDeveloper();
        var profile = dev.getProfile();
        if (profile == null) return null;

        // Busca tarifa vigente actual (año anterior al que se va a aplicar)
        var rateOpt = rateRepository
                .findTopByClientIdAndDeveloperProfileIdAndStatusOrderByValidFromDesc(
                        assignment.getClient().getId(), profile.getId(),
                        RateEntity.RateStatus.ACTIVE);

        if (rateOpt.isEmpty()) return null;

        RateEntity rate = rateOpt.get();
        BigDecimal current = rate.getMonthlyRate() != null
                ? rate.getMonthlyRate() : BigDecimal.ZERO;
        BigDecimal newRate = current.multiply(factor)
                .setScale(0, RoundingMode.HALF_UP); // sin decimales en COP

        return new DeveloperIncrementLineDto(
                dev.getId(), dev.getFullName(), profile.getName(),
                rate.getId(),
                current,
                current.divide(DIAS_MES,  0, RoundingMode.HALF_UP),
                current.divide(HORAS_MES, 0, RoundingMode.HALF_UP),
                newRate,
                newRate.divide(DIAS_MES,  0, RoundingMode.HALF_UP),
                newRate.divide(HORAS_MES, 0, RoundingMode.HALF_UP),
                newRate.subtract(current)
        );
    }

    // ---- Aprobar ----

    @Transactional
    public TariffIncrementEntity approve(ApproveIncrementRequestDto request) {

        var ipcRate = ipcRateRepository.findById(request.ipcRateId())
                .orElseThrow(() -> new ResourceNotFoundException("IpcRate", request.ipcRateId()));

        // Verifica que no se haya aprobado ya para este cliente y año
        if (incrementRepository.existsByClientIdAndApplyYearAndStatus(
                request.clientId(), request.applyYear(),
                TariffIncrementEntity.IncrementStatus.APPROVED)) {
            throw new BusinessException(
                    "Ya existe un incremento aprobado para este cliente en el año "
                            + request.applyYear());
        }

        // Simula de nuevo para obtener las líneas
        BigDecimal factor = BigDecimal.ONE.add(
                ipcRate.getIpcPercentage().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));

        var assignments = assignmentRepository.findActiveByClientWithDeveloper(request.clientId());
        LocalDate effectiveDate = LocalDate.of(request.applyYear(), 1, 1);

        for (ClientDeveloperAssignmentEntity assignment : assignments) {
            var dev     = assignment.getDeveloper();
            var profile = dev.getProfile();
            if (profile == null) continue;

            var rateOpt = rateRepository
                    .findTopByClientIdAndDeveloperProfileIdAndStatusOrderByValidFromDesc(
                            request.clientId(), profile.getId(), RateEntity.RateStatus.ACTIVE);
            if (rateOpt.isEmpty()) continue;

            RateEntity currentRate = rateOpt.get();
            BigDecimal newMonthly  = currentRate.getMonthlyRate()
                    .multiply(factor).setScale(0, RoundingMode.HALF_UP);

            // Vencer tarifa actual el 31 diciembre del año anterior
            currentRate.setValidUntil(LocalDate.of(request.applyYear() - 1, 12, 31));
            currentRate.setStatus(RateEntity.RateStatus.INACTIVE);
            rateRepository.save(currentRate);

            // Crear nueva tarifa con valor incrementado
            RateEntity newRate = RateEntity.builder()
                    .client(currentRate.getClient())
                    .developerProfile(profile)
                    .currency(currentRate.getCurrency())
                    .rateType(currentRate.getRateType())
                    .monthlyRate(newMonthly)
                    .validFrom(effectiveDate)
                    .validUntil(LocalDate.of(request.applyYear(), 12, 31))
                    .includesTax(currentRate.isIncludesTax())
                    .taxPercentage(currentRate.getTaxPercentage())
                    .discountPercentage(currentRate.getDiscountPercentage())
                    .workingHoursPerDay(currentRate.getWorkingHoursPerDay())
                    .status(RateEntity.RateStatus.ACTIVE)
                    .commercialNotes("Incremento IPC " + ipcRate.getIpcPercentage() + "% año " + request.applyYear())
                    .build();
            rateRepository.save(newRate);

            log.info("Tarifa incrementada developer={} {} -> {}",
                    dev.getFullName(), currentRate.getMonthlyRate(), newMonthly);
        }

        // Guardar registro del incremento aprobado
        var client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        TariffIncrementEntity increment = TariffIncrementEntity.builder()
                .client(client)
                .ipcRate(ipcRate)
                .applyYear(request.applyYear())
                .effectiveDate(effectiveDate)
                .ipcPercentage(ipcRate.getIpcPercentage())
                .status(TariffIncrementEntity.IncrementStatus.APPROVED)
                .approvedBy(currentUser())
                .observations(request.observations())
                .build();

        return incrementRepository.save(increment);
    }

    @Transactional(readOnly = true)
    public List<TariffIncrementEntity> findHistory(UUID clientId) {
        return incrementRepository.findByClientIdOrderByApplyYearDesc(clientId);
    }

    private IpcRateResponseDto toIpcDto(IpcRateEntity e) {
        return new IpcRateResponseDto(
                e.getId(), e.getYear(), e.getIpcPercentage(),
                e.getDescription(), e.getSource(), e.getCreatedAt());
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
