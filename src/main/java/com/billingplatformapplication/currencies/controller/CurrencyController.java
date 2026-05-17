package com.billingplatformapplication.currencies.controller;




import com.billingplatformapplication.currencies.dto.request.CreateCurrencyRequestDto;
import com.billingplatformapplication.currencies.dto.response.CurrencyResponseDto;
import com.billingplatformapplication.currencies.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currencies", description = "Currency management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active currencies")
    public ResponseEntity<List<CurrencyResponseDto>> findAll() {
        return ResponseEntity.ok(currencyService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CurrencyResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(currencyService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create currency")
    public ResponseEntity<CurrencyResponseDto> create(
            @Valid @RequestBody CreateCurrencyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(currencyService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate currency")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        currencyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

