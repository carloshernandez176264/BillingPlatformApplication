package com.billingplatformapplication.developers.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateDeveloperRequestDto(
        @NotBlank @Size(max = 30) String documentId,
        @NotBlank @Size(max = 20) String documentType,
        @NotBlank @Size(max = 200) String fullName,
        @Email @Size(max = 150) String email,
        @NotNull UUID profileId,
        @NotNull @PastOrPresent LocalDate hireDate,
        @Size(max = 50) String assignmentMode,
        @DecimalMin("0.0") BigDecimal baseSalary
) {
}
