package com.billingplatformapplication.worklogs.service;

import com.billingplatformapplication.billing.factory.RateStrategyFactory;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.client.service.ClientService;
import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.developerprofiles.service.DeveloperProfileService;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.developers.service.DeveloperService;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.rates.service.RateService;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import com.billingplatformapplication.worklogs.dto.request.CreateWorkLogRequestDto;
import com.billingplatformapplication.worklogs.dto.response.WorkLogResponseDto;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import com.billingplatformapplication.worklogs.mapper.WorkLogMapper;
import com.billingplatformapplication.worklogs.repository.WorkLogRepository;
import com.billingplatformapplication.worklogs.validator.WorkLogValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository    workLogRepository;
    private final ClientService        clientService;
    private final DeveloperService     developerService;
    private final DeveloperProfileService profileService;
    private final RateService          rateService;
    private final RateStrategyFactory  strategyFactory;
    private final WorkLogMapper        workLogMapper;
    private final WorkLogValidator     workLogValidator;

    @Transactional(readOnly = true)
    public PageResponseDto<WorkLogResponseDto> search(UUID clientId, UUID developerId,
                                                      Integer year, Integer month,
                                                      Pageable pageable) {
        return PageResponseDto.from(
                workLogRepository.searchWithFilters(clientId, developerId, year, month, pageable)
                        .map(workLogMapper::toDto));
    }

    @Transactional(readOnly = true)
    public WorkLogResponseDto findById(UUID id) {
        return workLogMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public WorkLogEntity findEntityById(UUID id) {
        return workLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkLog", id));
    }

    @Transactional
    public WorkLogResponseDto create(CreateWorkLogRequestDto request) {
        workLogValidator.validateCreate(request);

        ClientEntity           client  = clientService.findEntityById(request.clientId());
        DeveloperEntity        dev     = developerService.findEntityById(request.developerId());
        DeveloperProfileEntity profile = profileService.findEntityById(request.developerProfileId());

        WorkLogEntity entity = workLogMapper.toEntity(request);
        entity.setClient(client);
        entity.setDeveloper(dev);
        entity.setDeveloperProfile(profile);

        Optional<RateEntity> rateOpt = rateService.findApplicableRate(
                client.getId(), profile.getId(),
                request.billingYear(), request.billingMonth());

        rateOpt.ifPresentOrElse(
                rate -> {
                    entity.setAppliedRate(rate);
                    entity.setBillableAmount(
                            strategyFactory.getStrategy(rate.getRateType())
                                    .calculate(entity, rate));
                },
                () -> log.warn("No rate found for client={} profile={} period={}/{}",
                        client.getId(), profile.getId(),
                        request.billingYear(), request.billingMonth()));

        return workLogMapper.toDto(workLogRepository.save(entity));
    }

    /**
     * Busca un work_log existente para el período.
     * Si no existe, lo crea automáticamente con los valores de la tarifa vigente.
     * Usado por BillingNoveltyService para garantizar que siempre haya work_log.
     */
    @Transactional
    public WorkLogEntity findOrCreateForNovelty(UUID clientId, UUID developerId,
                                                int year, int month) {
        // 1. Buscar si ya existe
        Optional<WorkLogEntity> existing =
                workLogRepository.findByClientDeveloperAndPeriod(
                        clientId, developerId, year, month);

        if (existing.isPresent()) {
            log.info("WorkLog existente encontrado para developer={} period={}/{}",
                    developerId, year, month);
            return existing.get();
        }

        // 2. No existe — crear automáticamente
        log.info("Creando WorkLog automático para developer={} period={}/{}", developerId, year, month);

        ClientEntity           client  = clientService.findEntityById(clientId);
        DeveloperEntity        dev     = developerService.findEntityById(developerId);
        DeveloperProfileEntity profile = dev.getProfile();

        if (profile == null) {
            throw new BusinessException(
                    "El desarrollador no tiene perfil asignado — no se puede crear el registro de horas");
        }

        // 3. Buscar tarifa vigente
        RateEntity rate = rateService.findApplicableRate(
                        clientId, profile.getId(), year, month)
                .orElseThrow(() -> new BusinessException(
                        "No existe tarifa vigente para el desarrollador '" +
                                dev.getFullName() + "' en el período " + year + "/" + month +
                                ". Crea la tarifa primero."));

        // 4. Calcular días hábiles y horas del mes según la tarifa
        BigDecimal hoursPerDay     = rate.getWorkingHoursPerDay() != null
                ? rate.getWorkingHoursPerDay() : new BigDecimal("8");
        int        workingDays     = calcularDiasHabiles(year, month);
        BigDecimal expectedHours   = hoursPerDay.multiply(
                new BigDecimal(workingDays)).setScale(2, RoundingMode.HALF_UP);

        // 5. Construir y guardar el work_log
        WorkLogEntity entity = WorkLogEntity.builder()
                .client(client)
                .developer(dev)
                .developerProfile(profile)
                .appliedRate(rate)
                .billingYear(year)
                .billingMonth(month)
                .expectedWorkingDays(workingDays)
                .expectedWorkingHours(expectedHours)
                .actualWorkedHours(expectedHours)   // inicia con horas completas
                .status(WorkLogEntity.WorkLogStatus.DRAFT)
                .observations("Registro creado automáticamente por el sistema al registrar una novedad")
                .build();

        // 6. Calcular monto facturable inicial (sin descuentos aún)
        BigDecimal billable = strategyFactory.getStrategy(rate.getRateType())
                .calculate(entity, rate);
        entity.setBillableAmount(billable);

        WorkLogEntity saved = workLogRepository.save(entity);
        log.info("WorkLog automático creado id={} para developer={} period={}/{}",
                saved.getId(), developerId, year, month);
        return saved;
    }

    @Transactional
    public WorkLogResponseDto confirm(UUID id) {
        WorkLogEntity entity = findEntityById(id);
        if (entity.getStatus() != WorkLogEntity.WorkLogStatus.DRAFT) {
            throw new BusinessException("Solo se pueden confirmar registros en estado DRAFT");
        }
        entity.setStatus(WorkLogEntity.WorkLogStatus.CONFIRMED);
        return workLogMapper.toDto(workLogRepository.save(entity));
    }

    @Transactional
    public void deactivate(UUID id) {
        WorkLogEntity entity = findEntityById(id);
        if (entity.getStatus() == WorkLogEntity.WorkLogStatus.BILLED) {
            throw new BusinessException("No se puede desactivar un registro ya facturado");
        }
        entity.setActive(false);
        workLogRepository.save(entity);
    }

    /**
     * Calcula los días hábiles del mes (lunes a viernes).
     * Se puede sobrescribir con festivos en el futuro.
     */
    private int calcularDiasHabiles(int year, int month) {
        YearMonth ym   = YearMonth.of(year, month);
        int       days = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            java.time.DayOfWeek dow = ym.atDay(d).getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY &&
                    dow != java.time.DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }
}