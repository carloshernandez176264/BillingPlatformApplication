package com.billingplatformapplication.client.service;


import com.billingplatformapplication.audit.service.AuditService;
import com.billingplatformapplication.client.dto.request.CreateClientRequestDto;
import com.billingplatformapplication.client.dto.request.UpdateClientRequestDto;
import com.billingplatformapplication.client.dto.response.ClientResponseDto;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.client.mapper.ClientMapper;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.client.validator.ClientValidator;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.currencies.service.CurrencyService;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final CurrencyService currencyService;
    private final ClientMapper clientMapper;
    private final ClientValidator clientValidator;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponseDto<ClientResponseDto> findAll(String search, String status,
                                                      Pageable pageable) {
        ClientEntity.ClientStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = ClientEntity.ClientStatus.valueOf(status.toUpperCase());
        }
        return PageResponseDto.from(
                clientRepository.searchWithFilters(search, statusEnum, pageable)
                        .map(clientMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ClientResponseDto findById(UUID id) {
        return clientMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public ClientEntity findEntityById(UUID id) {
        return clientRepository.findByIdWithCurrency(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    @Transactional
    public ClientResponseDto create(CreateClientRequestDto request) {
        clientValidator.validateCreate(request);
        CurrencyEntity currency = currencyService.findEntityById(request.primaryCurrencyId());
        ClientEntity entity = clientMapper.toEntity(request);
        entity.setPrimaryCurrency(currency);
        ClientEntity saved = clientRepository.save(entity);
        auditService.log("CLIENT", saved.getId().toString(), "CREATE",
                currentUser(), null,
                Map.of("taxId", saved.getTaxId(), "companyName", saved.getCompanyName()));
        return clientMapper.toDto(saved);
    }

    @Transactional
    public ClientResponseDto update(UUID id, UpdateClientRequestDto request) {
        ClientEntity entity = findEntityById(id);
        clientMapper.updateEntity(request, entity);
        if (request.primaryCurrencyId() != null) {
            entity.setPrimaryCurrency(
                    currencyService.findEntityById(request.primaryCurrencyId()));
        }
        if (request.status() != null) {
            entity.setStatus(ClientEntity.ClientStatus.valueOf(request.status().toUpperCase()));
        }
        ClientEntity saved = clientRepository.save(entity);
        auditService.log("CLIENT", id.toString(), "UPDATE", currentUser(), null, null);
        return clientMapper.toDto(saved);
    }

    @Transactional
    public void deactivate(UUID id) {
        ClientEntity entity = findEntityById(id);
        entity.setActive(false);
        entity.setStatus(ClientEntity.ClientStatus.INACTIVE);
        clientRepository.save(entity);
        auditService.log("CLIENT", id.toString(), "DELETE", currentUser(), null, null);
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}

