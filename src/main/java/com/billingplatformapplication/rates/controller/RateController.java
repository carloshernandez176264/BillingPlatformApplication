package com.billingplatformapplication.rates.controller;

import com.billingplatformapplication.rates.dto.request.CreateRateRequestDto;
import com.billingplatformapplication.rates.dto.response.RateResponseDto;
import com.billingplatformapplication.rates.service.RateService;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rates")
@Tag(name = "Rates", description = "Billing rate management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class RateController {

    private final RateService rateService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_RATE')")
    public ResponseEntity<PageResponseDto<RateResponseDto>> search(
            @RequestParam(required = false) UUID   clientId,
            @RequestParam(required = false) UUID   profileId,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(rateService.search(clientId, profileId, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_RATE')")
    public ResponseEntity<RateResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(rateService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_RATE')")
    @Operation(summary = "Create a billing rate — validates overlap and business rules")
    public ResponseEntity<RateResponseDto> create(
            @Valid @RequestBody CreateRateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rateService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_RATE')")
    @Operation(summary = "Deactivate a rate — soft delete")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        rateService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

