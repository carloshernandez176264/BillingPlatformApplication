package com.billingplatformapplication.preinvoices.service;



import com.billingplatformapplication.audit.service.AuditService;
import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.service.BillingCalculationService;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.client.service.ClientService;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.currencies.service.CurrencyService;
import com.billingplatformapplication.preinvoices.builder.PreInvoiceBuilder;
import com.billingplatformapplication.preinvoices.dto.request.GeneratePreInvoiceRequestDto;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceEntity;
import com.billingplatformapplication.preinvoices.mapper.PreInvoiceMapper;
import com.billingplatformapplication.preinvoices.repository.PreInvoiceRepository;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreInvoiceService {

    private final
    PreInvoiceRepository preInvoiceRepository;
    private final BillingCalculationService billingCalculationService;
    private final ClientService clientService;
    private final CurrencyService currencyService;
    private final PreInvoiceBuilder preInvoiceBuilder;
    private final PreInvoiceMapper preInvoiceMapper;
    private final AuditService auditService;

    // ---------------------------------------------------------------- Query

    @Transactional(readOnly = true)
    public PreInvoiceResponseDto findById(UUID id) {
        return preInvoiceMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public PageResponseDto<PreInvoiceResponseDto> search(UUID clientId, String status,
                                                         Integer year, Integer month,
                                                         Pageable pageable) {
        PreInvoiceEntity.PreInvoiceStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = PreInvoiceEntity.PreInvoiceStatus.valueOf(status.toUpperCase());
        }
        return PageResponseDto.from(
                preInvoiceRepository.searchWithFilters(clientId, statusEnum, year, month, pageable)
                        .map(preInvoiceMapper::toDto));
    }

    // ---------------------------------------------------------------- Generate

    /**
     * Generates a pre-invoice by calling BillingCalculationService.
     * The backend calculates ALL amounts — Angular only displays the result.
     */
    @Transactional
    public PreInvoiceResponseDto generate(GeneratePreInvoiceRequestDto request) {
        ClientEntity client   = clientService.findEntityById(request.clientId());
        CurrencyEntity currency = currencyService.findEntityById(request.currencyId());

        BillingCalculationResultDto calc = billingCalculationService
                .calculateBilling(client.getId(), request.billingYear(), request.billingMonth());

        if (calc.getLines().isEmpty()) {
            throw new BusinessException(
                    "No billable work logs found for client '" + client.getCompanyName() +
                            "' in period " + request.billingYear() + "/" + request.billingMonth());
        }

        String invoiceNumber = nextInvoiceNumber();
        PreInvoiceEntity invoice = preInvoiceBuilder.build(
                invoiceNumber, client, currency,
                request.billingYear(), request.billingMonth(),
                calc.getLines(), request.observations());

        // Override totals with exact calculated values (tax included)
        invoice.setTaxableAmount(calc.getTaxableAmount());
        invoice.setTaxAmount(calc.getTaxAmount());
        invoice.setTotalAmount(calc.getTotalAmount());
        invoice.setTotalNoveltyDiscounts(calc.getTotalNoveltyDiscounts());
        invoice.setTotalOtherDiscounts(calc.getTotalOtherDiscounts());
        invoice.setSubtotal(calc.getSubtotal());

        PreInvoiceEntity saved = preInvoiceRepository.save(invoice);
        auditService.log("PRE_INVOICE", saved.getId().toString(), "GENERATE",
                currentUser(), null,
                Map.of("invoiceNumber", invoiceNumber,
                        "totalAmount", calc.getTotalAmount().toString()));
        log.info("Pre-invoice generated: {} client={} period={}/{}",
                invoiceNumber, client.getCompanyName(),
                request.billingYear(), request.billingMonth());
        return preInvoiceMapper.toDto(saved);
    }

    // ---------------------------------------------------------------- State transitions

    @Transactional
    public PreInvoiceResponseDto approve(UUID id, String observations) {
        PreInvoiceEntity entity = getEditableOrThrow(id,
                PreInvoiceEntity.PreInvoiceStatus.GENERATED,
                PreInvoiceEntity.PreInvoiceStatus.SENT_TO_CLIENT);
        entity.setStatus(PreInvoiceEntity.PreInvoiceStatus.APPROVED);
        if (observations != null) entity.setObservations(observations);
        auditService.log("PRE_INVOICE", id.toString(), "APPROVE", currentUser(), null, null);
        return preInvoiceMapper.toDto(preInvoiceRepository.save(entity));
    }

    @Transactional
    public PreInvoiceResponseDto reject(UUID id, String reason) {
        requireNonBlank(reason, "Rejection reason");
        PreInvoiceEntity entity = getEntityById(id);
        requireStatusIn(entity,
                PreInvoiceEntity.PreInvoiceStatus.GENERATED,
                PreInvoiceEntity.PreInvoiceStatus.SENT_TO_CLIENT);
        entity.setStatus(PreInvoiceEntity.PreInvoiceStatus.REJECTED);
        entity.setRejectionReason(reason);
        auditService.log("PRE_INVOICE", id.toString(), "REJECT", currentUser(),
                null, Map.of("reason", reason));
        return preInvoiceMapper.toDto(preInvoiceRepository.save(entity));
    }

    @Transactional
    public PreInvoiceResponseDto sendToClient(UUID id) {
        PreInvoiceEntity entity = getEditableOrThrow(id,
                PreInvoiceEntity.PreInvoiceStatus.GENERATED);
        entity.setStatus(PreInvoiceEntity.PreInvoiceStatus.SENT_TO_CLIENT);
        auditService.log("PRE_INVOICE", id.toString(), "SEND", currentUser(), null, null);
        return preInvoiceMapper.toDto(preInvoiceRepository.save(entity));
    }

    @Transactional
    public PreInvoiceResponseDto cancel(UUID id, String reason) {
        requireNonBlank(reason, "Cancellation reason");
        PreInvoiceEntity entity = getEntityById(id);
        if (entity.getStatus() == PreInvoiceEntity.PreInvoiceStatus.INVOICED) {
            throw new BusinessException("Cannot cancel an already invoiced pre-invoice");
        }
        entity.setStatus(PreInvoiceEntity.PreInvoiceStatus.CANCELLED);
        entity.setRejectionReason(reason);
        auditService.log("PRE_INVOICE", id.toString(), "CANCEL", currentUser(),
                null, Map.of("reason", reason));
        return preInvoiceMapper.toDto(preInvoiceRepository.save(entity));
    }

    // ---------------------------------------------------------------- Helpers

    private PreInvoiceEntity findEntityById(UUID id) {
        return preInvoiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("PreInvoice", id));
    }

    private PreInvoiceEntity getEntityById(UUID id) {
        return findEntityById(id);
    }

    private PreInvoiceEntity getEditableOrThrow(UUID id,
                                                PreInvoiceEntity.PreInvoiceStatus... allowed) {
        PreInvoiceEntity entity = findEntityById(id);
        requireStatusIn(entity, allowed);
        return entity;
    }

    private void requireStatusIn(PreInvoiceEntity entity,
                                 PreInvoiceEntity.PreInvoiceStatus... allowed) {
        for (PreInvoiceEntity.PreInvoiceStatus s : allowed) {
            if (entity.getStatus() == s) return;
        }
        throw new BusinessException(
                "Pre-invoice '" + entity.getInvoiceNumber() +
                        "' is in status " + entity.getStatus() +
                        " and cannot be transitioned to the requested state");
    }

    private String nextInvoiceNumber() {
        Integer seq = preInvoiceRepository.findMaxInvoiceSequence();
        return String.format("PRE%08d", (seq != null ? seq : 0) + 1);
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + " is required");
        }
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}

