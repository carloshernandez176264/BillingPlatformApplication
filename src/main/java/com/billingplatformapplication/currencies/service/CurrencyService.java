package com.billingplatformapplication.currencies.service;


import com.billingplatformapplication.currencies.dto.request.CreateCurrencyRequestDto;
import com.billingplatformapplication.currencies.dto.response.CurrencyResponseDto;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.currencies.mapper.CurrencyMapper;
import com.billingplatformapplication.currencies.repository.CurrencyRepository;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @Transactional(readOnly = true)
    public List<CurrencyResponseDto> findAll() {
        return currencyMapper.toDtoList(currencyRepository.findByActiveTrue());
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDto findById(UUID id) {
        return currencyMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public CurrencyEntity findEntityById(UUID id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    @Transactional(readOnly = true)
    public CurrencyEntity findEntityByCode(String code) {
        return currencyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + code));
    }

    @Transactional
    public CurrencyResponseDto create(CreateCurrencyRequestDto request) {
        if (currencyRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Currency", "code", request.code());
        }
        return currencyMapper.toDto(
                currencyRepository.save(currencyMapper.toEntity(request)));
    }

    @Transactional
    public void deactivate(UUID id) {
        CurrencyEntity entity = findEntityById(id);
        entity.setActive(false);
        currencyRepository.save(entity);
    }
}

