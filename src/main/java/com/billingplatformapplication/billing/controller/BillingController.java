package com.billingplatformapplication.billing.controller;

import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.usecase.BillingCalculationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
@Tag(name = "Billing", description = "Billing calculation — all numbers calculated server-side")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BillingController {

    private final BillingCalculationUseCase billingCalculationUseCase;

    @GetMapping("/calculate")
    @Operation(summary = "Calculate billing for a client/period — read-only, no side effects")
    @PreAuthorize("hasAuthority('GENERATE_PRE_INVOICE') or hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<BillingCalculationResultDto> calculate(
            @RequestParam UUID clientId,
            @RequestParam int  year,
            @RequestParam int  month) {
        return ResponseEntity.ok(billingCalculationUseCase.execute(clientId, year, month));
    }
}

