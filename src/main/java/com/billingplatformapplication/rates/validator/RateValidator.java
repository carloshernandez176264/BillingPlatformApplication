package com.billingplatformapplication.rates.validator;


import com.billingplatformapplication.rates.dto.request.CreateRateRequestDto;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.rates.repository.RateRepository;
import com.billingplatformapplication.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateValidator {

    private final RateRepository rateRepository;

    private static final UUID EMPTY_UUID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    public void validateCreate(CreateRateRequestDto request) {
        validateRateValue(request);
        validateDateRange(request);
        validateNoOverlap(request, EMPTY_UUID);
    }

    public void validateUpdate(CreateRateRequestDto request, UUID excludeId) {
        validateRateValue(request);
        validateDateRange(request);
        validateNoOverlap(request, excludeId);
    }

    private void validateRateValue(CreateRateRequestDto request) {
        RateEntity.RateType type = RateEntity.RateType.valueOf(request.rateType().toUpperCase());
        switch (type) {
            case MONTHLY -> require(request.monthlyRate(), "Monthly rate");
            case DAILY   -> require(request.dailyRate(),   "Daily rate");
            case HOURLY  -> require(request.hourlyRate(),  "Hourly rate");
        }
    }

    private void validateDateRange(CreateRateRequestDto request) {
        if (request.validUntil() != null
                && request.validUntil().isBefore(request.validFrom())) {
            throw new BusinessException("validUntil must be after validFrom");
        }
    }

    private void validateNoOverlap(CreateRateRequestDto request, UUID excludeId) {
        boolean overlaps = rateRepository.existsOverlapping(
                request.clientId(),
                request.developerProfileId(),
                request.currencyId(),
                request.validFrom(),
                request.validUntil(),
                excludeId);

        if (overlaps) {
            throw new BusinessException(
                    "An active rate already exists for this client/profile/currency " +
                            "combination overlapping the requested period");
        }
    }

    private void require(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(fieldName + " must be provided and positive");
        }
    }
}

