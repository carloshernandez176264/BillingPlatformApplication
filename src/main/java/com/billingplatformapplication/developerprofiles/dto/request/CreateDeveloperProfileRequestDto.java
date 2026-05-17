package com.billingplatformapplication.developerprofiles.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateDeveloperProfileRequestDto(
        @NotBlank(message = "El nombre del perfil es obligatorio")
        @Size(max = 100)
        String name,

        @Size(max = 30)
        String level,

        String description,
        String baseSkills,

        BigDecimal baseMonthlyRate  // tarifa base interna por perfil
) {}