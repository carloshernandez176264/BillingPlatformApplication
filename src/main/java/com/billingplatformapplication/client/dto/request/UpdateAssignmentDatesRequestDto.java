package com.billingplatformapplication.client.dto.request;

import java.time.LocalDate;

public record UpdateAssignmentDatesRequestDto(
        LocalDate startDate,
        LocalDate endDate
) {}