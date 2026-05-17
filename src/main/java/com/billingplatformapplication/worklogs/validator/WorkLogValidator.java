package com.billingplatformapplication.worklogs.validator;

import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import com.billingplatformapplication.worklogs.dto.request.CreateWorkLogRequestDto;
import com.billingplatformapplication.worklogs.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WorkLogValidator {

    private final WorkLogRepository workLogRepository;

    @Value("${billing.max-hours-per-month:300}")
    private BigDecimal maxHoursPerMonth;

    public void validateCreate(CreateWorkLogRequestDto request) {
        if (request.actualWorkedHours().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Actual worked hours cannot be negative");
        }
        if (request.actualWorkedHours().compareTo(maxHoursPerMonth) > 0) {
            throw new BusinessException(
                    "Actual worked hours exceed maximum allowed per month: " + maxHoursPerMonth);
        }
        boolean exists = workLogRepository
                .existsByClientIdAndDeveloperIdAndDeveloperProfileIdAndBillingYearAndBillingMonth(
                        request.clientId(), request.developerId(),
                        request.developerProfileId(),
                        request.billingYear(), request.billingMonth());
        if (exists) {
            throw new DuplicateResourceException(
                    "A work log already exists for this client/developer/profile " +
                            "in period " + request.billingYear() + "/" + request.billingMonth());
        }
    }
}
