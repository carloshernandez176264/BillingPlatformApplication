package com.billingplatformapplication.profitability.controller;

import com.billingplatformapplication.profitability.dto.response.ProfitabilityResponseDto;
import com.billingplatformapplication.profitability.service.ProfitabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profitability")
@Tag(name = "Rentabilidad", description = "Cálculo de rentabilidad por desarrollador y cliente")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProfitabilityController {

    private final ProfitabilityService profitabilityService;

    @GetMapping("/by-client")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('VIEW_REPORTS')")
    public List<ProfitabilityResponseDto> byClient(@RequestParam UUID clientId) {
        return profitabilityService.calculateByClient(clientId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('VIEW_REPORTS')")
    public List<ProfitabilityResponseDto> all() {
        return profitabilityService.calculateAll();
    }
}