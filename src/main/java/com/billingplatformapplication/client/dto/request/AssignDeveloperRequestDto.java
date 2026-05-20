package com.billingplatformapplication.client.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AssignDeveloperRequestDto(
        @NotNull UUID developerId,
        String notes,
        LocalDate startDate,
        LocalDate endDate
) {}