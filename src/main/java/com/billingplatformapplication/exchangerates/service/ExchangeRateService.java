package com.billingplatformapplication.exchangerates.service;


import com.billingplatformapplication.currencies.service.CurrencyService;
import com.billingplatformapplication.exchangerates.dto.request.CreateExchangeRateRequestDto;
import com.billingplatformapplication.exchangerates.dto.response.ExchangeRateResponseDto;
import com.billingplatformapplication.exchangerates.entity.ExchangeRateEntity;
import com.billingplatformapplication.exchangerates.mapper.ExchangeRateMapper;
import com.billingplatformapplication.exchangerates.repository.ExchangeRateRepository;
import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyService currencyService;
    private final ExchangeRateMapper exchangeRateMapper;

    @Transactional
    public ExchangeRateResponseDto create(CreateExchangeRateRequestDto request) {
        if (request.fromCurrencyId().equals(request.toCurrencyId())) {
            throw new BusinessException("From and To currencies must be different");
        }
        if (request.validUntil() != null && request.validUntil().isBefore(request.validFrom())) {
            throw new BusinessException("validUntil must be after validFrom");
        }
        if (exchangeRateRepository.existsByFromCurrencyIdAndToCurrencyIdAndValidFrom(
                request.fromCurrencyId(), request.toCurrencyId(), request.validFrom())) {
            throw new DuplicateResourceException(
                    "Exchange rate already exists for this currency pair and date");
        }
        ExchangeRateEntity entity = exchangeRateMapper.toEntity(request);
        entity.setFromCurrency(currencyService.findEntityById(request.fromCurrencyId()));
        entity.setToCurrency(currencyService.findEntityById(request.toCurrencyId()));
        return exchangeRateMapper.toDto(exchangeRateRepository.save(entity));
    }

    /**
     * Converts an amount from one currency to another using the applicable rate on a given date.
     * Returns the same amount when currencies are equal — no conversion needed.
     */
    @Transactional(readOnly = true)
    public BigDecimal convert(BigDecimal amount, UUID fromCurrencyId,
                              UUID toCurrencyId, LocalDate date) {
        if (fromCurrencyId.equals(toCurrencyId)) return amount;

        ExchangeRateEntity rate = exchangeRateRepository
                .findApplicableRate(fromCurrencyId, toCurrencyId, date)
                .orElseThrow(() -> new BusinessException(
                        "No exchange rate found for currency pair on " + date));

        return amount.multiply(rate.getRate()).setScale(4, RoundingMode.HALF_UP);
    }
}

