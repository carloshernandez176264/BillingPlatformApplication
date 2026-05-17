package com.billingplatformapplication.rates.service;


import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.client.service.ClientService;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.currencies.service.CurrencyService;
import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.developerprofiles.service.DeveloperProfileService;
import com.billingplatformapplication.rates.dto.request.CreateRateRequestDto;
import com.billingplatformapplication.rates.dto.response.RateResponseDto;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.rates.mapper.RateMapper;
import com.billingplatformapplication.rates.repository.RateRepository;
import com.billingplatformapplication.rates.validator.RateValidator;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;
    private final ClientService clientService;
    private final DeveloperProfileService profileService;
    private final CurrencyService currencyService;
    private final RateMapper rateMapper;
    private final RateValidator rateValidator;

    @Transactional(readOnly = true)
    public PageResponseDto<RateResponseDto> search(UUID clientId, UUID profileId,
                                                   String status, Pageable pageable) {
        RateEntity.RateStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = RateEntity.RateStatus.valueOf(status.toUpperCase());
        }
        return PageResponseDto.from(
                rateRepository.searchWithFilters(clientId, profileId, statusEnum, pageable)
                        .map(rateMapper::toDto));
    }

    @Transactional(readOnly = true)
    public RateResponseDto findById(UUID id) {
        return rateMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public RateEntity findEntityById(UUID id) {
        return rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate", id));
    }

    /**
     * Returns the most specific applicable rate for a client/profile/period.
     * Client-specific rate takes priority over base rate.
     */
    @Transactional(readOnly = true)
    public Optional<RateEntity> findApplicableRate(UUID clientId, UUID profileId,
                                                   int year, int month) {
        LocalDate periodDate = LocalDate.of(year, month, 1);
        List<RateEntity> candidates =
                rateRepository.findApplicableRates(clientId, profileId, periodDate);
        return candidates.stream().findFirst();
    }

    @Transactional
    public RateResponseDto create(CreateRateRequestDto request) {
        rateValidator.validateCreate(request);

        DeveloperProfileEntity profile = profileService.findEntityById(request.developerProfileId());
        CurrencyEntity currency = currencyService.findEntityById(request.currencyId());
        ClientEntity client   = null;
        if (request.clientId() != null) {
            client = clientService.findEntityById(request.clientId());
        }

        RateEntity entity = rateMapper.toEntity(request);
        entity.setDeveloperProfile(profile);
        entity.setCurrency(currency);
        entity.setClient(client);

        if (entity.getDiscountPercentage() == null) {
            entity.setDiscountPercentage(BigDecimal.ZERO);
        }
        if (entity.getWorkingHoursPerDay() == null) {
            entity.setWorkingHoursPerDay(new BigDecimal("8.00"));
        }

        return rateMapper.toDto(rateRepository.save(entity));
    }

    @Transactional
    public void deactivate(UUID id) {
        RateEntity entity = findEntityById(id);
        entity.setActive(false);
        entity.setStatus(RateEntity.RateStatus.INACTIVE);
        rateRepository.save(entity);
    }
}
