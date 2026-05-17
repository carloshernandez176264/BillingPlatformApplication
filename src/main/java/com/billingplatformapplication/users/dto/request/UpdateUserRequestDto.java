package com.billingplatformapplication.users.dto.request;

import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateUserRequestDto(
        @Size(max = 200)
        String fullName,

        String status,          // ACTIVE | INACTIVE | BLOCKED | SUSPENDED

        Set<UUID> roleIds,

        Boolean locked,

        Boolean mustChangePassword
) {}
