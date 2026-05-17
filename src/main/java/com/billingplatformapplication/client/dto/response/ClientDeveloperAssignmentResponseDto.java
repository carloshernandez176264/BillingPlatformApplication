package com.billingplatformapplication.client.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClientDeveloperAssignmentResponseDto(
        UUID id,
        UUID clientId,
        String clientName,
        UUID developerId,
        String developerName,
        String developerDocument,
        String profileName,
        boolean active,
        String notes,
        Instant createdAt
) {}
