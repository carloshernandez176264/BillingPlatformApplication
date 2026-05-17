package com.billingplatformapplication.users.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponseDto(
        UUID    id,
        String  email,
        String  fullName,
        String  status,
        boolean locked,
        boolean mustChangePassword,
        int     failedLoginAttempts,
        Instant lastLoginAt,
        Instant passwordChangedAt,
        Set<String> roles,
        boolean active,
        Instant createdAt,
        String  createdBy
) {}
