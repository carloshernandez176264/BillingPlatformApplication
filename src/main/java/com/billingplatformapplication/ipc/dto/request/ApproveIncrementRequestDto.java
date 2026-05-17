package com.billingplatformapplication.ipc.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApproveIncrementRequestDto(
        @NotNull UUID clientId,
        @NotNull Integer applyYear,
        @NotNull UUID ipcRateId,
        String observations
) {}
