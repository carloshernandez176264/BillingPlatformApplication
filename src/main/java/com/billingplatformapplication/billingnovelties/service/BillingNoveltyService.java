package com.billingplatformapplication.billingnovelties.service;

import com.billingplatformapplication.billing.calculator.NoveltyDiscountCalculator;
import com.billingplatformapplication.billingnovelties.dto.request.CreateBillingNoveltyRequestDto;
import com.billingplatformapplication.billingnovelties.dto.response.BillingNoveltyResponseDto;
import com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity;
import com.billingplatformapplication.billingnovelties.mapper.BillingNoveltyMapper;
import com.billingplatformapplication.billingnovelties.repository.BillingNoveltyRepository;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.client.service.ClientService;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.developers.service.DeveloperService;
import com.billingplatformapplication.rates.service.RateService;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import com.billingplatformapplication.worklogs.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingNoveltyService {

    private final BillingNoveltyRepository noveltyRepository;
    private final WorkLogService workLogService;
    private final ClientService clientService;
    private final DeveloperService developerService;
    private final RateService rateService;
    private final NoveltyDiscountCalculator discountCalculator;
    private final BillingNoveltyMapper noveltyMapper;

    @Transactional(readOnly = true)
    public PageResponseDto<BillingNoveltyResponseDto> search(UUID workLogId, UUID developerId,
                                                             String approvalStatus,
                                                             Pageable pageable) {
        BillingNoveltyEntity.ApprovalStatus statusEnum = null;
        if (approvalStatus != null && !approvalStatus.isBlank()) {
            statusEnum = BillingNoveltyEntity.ApprovalStatus.valueOf(
                    approvalStatus.toUpperCase());
        }
        return PageResponseDto.from(
                noveltyRepository.searchWithFilters(workLogId, developerId, statusEnum, pageable)
                        .map(noveltyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public List<BillingNoveltyResponseDto> findByWorkLog(UUID workLogId) {
        return noveltyMapper.toDtoList(noveltyRepository.findByWorkLogId(workLogId));
    }

    @Transactional(readOnly = true)
    public BillingNoveltyResponseDto findById(UUID id) {
        return noveltyMapper.toDto(findEntityById(id));
    }

    @Transactional
    public BillingNoveltyResponseDto create(CreateBillingNoveltyRequestDto request) {

        ClientEntity client = clientService.findEntityById(request.clientId());
        DeveloperEntity dev = developerService.findEntityById(request.developerId());

        // Garantiza que exista un work_log para el período.
        // Si no existe, lo crea automáticamente con los valores de la tarifa.
        WorkLogEntity workLog = workLogService.findOrCreateForNovelty(
                request.clientId(),
                request.developerId(),
                request.billingYear(),
                request.billingMonth()
        );

        BillingNoveltyEntity entity = noveltyMapper.toEntity(request);
        entity.setWorkLog(workLog);
        entity.setClient(client);
        entity.setDeveloper(dev);
        entity.setApprovalStatus(BillingNoveltyEntity.ApprovalStatus.PENDING);

        // Calcula descuento automáticamente con la tarifa vigente
        UUID profileId = dev.getProfile() != null ? dev.getProfile().getId() : null;
        if (profileId != null) {
            rateService.findApplicableRate(
                    client.getId(),
                    profileId,
                    request.billingYear(),
                    request.billingMonth()
            ).ifPresent(rate ->
                    entity.setCalculatedDiscount(
                            discountCalculator.calculateSingleDiscount(entity, rate))
            );
        }

        return noveltyMapper.toDto(noveltyRepository.save(entity));
    }

    @Transactional
    public BillingNoveltyResponseDto approve(UUID id, String approvedBy) {
        BillingNoveltyEntity entity = findEntityById(id);
        if (entity.getApprovalStatus() != BillingNoveltyEntity.ApprovalStatus.PENDING) {
            throw new BusinessException("Solo se pueden aprobar novedades en estado PENDING");
        }
        entity.setApprovalStatus(BillingNoveltyEntity.ApprovalStatus.APPROVED);
        entity.setApprovedBy(approvedBy != null ? approvedBy : currentUser());
        return noveltyMapper.toDto(noveltyRepository.save(entity));
    }

    @Transactional
    public BillingNoveltyResponseDto reject(UUID id, String reason) {
        BillingNoveltyEntity entity = findEntityById(id);
        if (entity.getApprovalStatus() != BillingNoveltyEntity.ApprovalStatus.PENDING) {
            throw new BusinessException("Solo se pueden rechazar novedades en estado PENDING");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("El motivo de rechazo es obligatorio");
        }
        entity.setApprovalStatus(BillingNoveltyEntity.ApprovalStatus.REJECTED);
        entity.setRejectionReason(reason);
        return noveltyMapper.toDto(noveltyRepository.save(entity));
    }

    @Transactional
    public void deactivate(UUID id) {
        BillingNoveltyEntity entity = findEntityById(id);
        if (entity.getApprovalStatus() == BillingNoveltyEntity.ApprovalStatus.APPROVED) {
            throw new BusinessException("No se puede desactivar una novedad aprobada");
        }
        entity.setActive(false);
        noveltyRepository.save(entity);
    }

    private BillingNoveltyEntity findEntityById(UUID id) {
        return noveltyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingNovelty", id));
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}