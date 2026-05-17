package com.billingplatformapplication.reports.controller;

import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.service.BillingCalculationService;
import com.billingplatformapplication.reports.dto.*;
import com.billingplatformapplication.reports.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Reportes financieros y de facturación")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService            reportsService;
    private final BillingCalculationService billingCalculationService;

    @GetMapping("/billing/period-summary")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<PeriodSummaryDto> periodSummary(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(reportsService.periodSummary(year, month));
    }

    @GetMapping("/billing/client-annual")
    @Operation(summary = "Reporte anual individual por cliente — solo pre-facturas APPROVED")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ClientAnnualReportDto> clientAnnual(
            @RequestParam UUID clientId,
            @RequestParam int  year) {
        return ResponseEntity.ok(reportsService.clientAnnualReport(clientId, year));
    }

    @GetMapping("/billing/general-annual")
    @Operation(summary = "Reporte anual general — todos los clientes, solo APPROVED")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<GeneralAnnualReportDto> generalAnnual(
            @RequestParam int year) {
        return ResponseEntity.ok(reportsService.generalAnnualReport(year));
    }

    @GetMapping("/billing/client")
    @PreAuthorize("hasAuthority('VIEW_REPORTS') or hasAuthority('GENERATE_PRE_INVOICE')")
    public ResponseEntity<BillingCalculationResultDto> clientBilling(
            @RequestParam UUID clientId,
            @RequestParam int  year,
            @RequestParam int  month) {
        return ResponseEntity.ok(
                billingCalculationService.calculateBilling(clientId, year, month));
    }
}