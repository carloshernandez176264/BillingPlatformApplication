package com.billingplatformapplication.users.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.UUID;

public record CreateUserRequestDto(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Full name is required")
        @Size(max = 200)
        String fullName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain uppercase, lowercase, digit and special character"
        )
        String password,

        @NotEmpty(message = "At least one role is required")
        Set<UUID> roleIds
) {}

