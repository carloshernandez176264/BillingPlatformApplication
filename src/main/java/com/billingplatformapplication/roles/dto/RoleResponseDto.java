package com.billingplatformapplication.roles.dto;

import java.util.UUID;

public record RoleResponseDto(UUID id, String name, String description, boolean active) {
}
