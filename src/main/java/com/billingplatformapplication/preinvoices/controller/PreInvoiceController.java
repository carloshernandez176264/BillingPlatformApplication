package com.billingplatformapplication.preinvoices.controller;


import com.billingplatformapplication.preinvoices.dto.request.GeneratePreInvoiceRequestDto;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import com.billingplatformapplication.preinvoices.export.ExcelExportService;
import com.billingplatformapplication.preinvoices.export.PdfExportService;
import com.billingplatformapplication.preinvoices.service.PreInvoiceService;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pre-invoices")
@Tag(name = "Pre-Invoices", description = "Pre-invoice generation, approval and export")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PreInvoiceController {

    private final PreInvoiceService preInvoiceService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;

    @GetMapping
    @PreAuthorize("hasAuthority('GENERATE_PRE_INVOICE')")
    public ResponseEntity<PageResponseDto<PreInvoiceResponseDto>> search(
            @RequestParam(required = false) UUID    clientId,
            @RequestParam(required = false) String  status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @ParameterObject @PageableDefault(size = 20, sort = "generationDate") Pageable pageable) {
        return ResponseEntity.ok(preInvoiceService.search(clientId, status, year, month, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GENERATE_PRE_INVOICE')")
    public ResponseEntity<PreInvoiceResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(preInvoiceService.findById(id));
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate pre-invoice — all amounts calculated server-side")
    @PreAuthorize("hasAuthority('GENERATE_PRE_INVOICE')")
    public ResponseEntity<PreInvoiceResponseDto> generate(
            @Valid @RequestBody GeneratePreInvoiceRequestDto request) {
        return ResponseEntity.ok(preInvoiceService.generate(request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPROVE_PRE_INVOICE')")
    public ResponseEntity<PreInvoiceResponseDto> approve(
            @PathVariable UUID id,
            @RequestParam(required = false) String observations) {
        return ResponseEntity.ok(preInvoiceService.approve(id, observations));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('APPROVE_PRE_INVOICE')")
    public ResponseEntity<PreInvoiceResponseDto> reject(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(preInvoiceService.reject(id, reason));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('GENERATE_PRE_INVOICE')")
    public ResponseEntity<PreInvoiceResponseDto> sendToClient(@PathVariable UUID id) {
        return ResponseEntity.ok(preInvoiceService.sendToClient(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('APPROVE_PRE_INVOICE')")
    public ResponseEntity<PreInvoiceResponseDto> cancel(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(preInvoiceService.cancel(id, reason));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pre-factura — solo DRAFT o CANCELLED")
    @PreAuthorize("hasAuthority('GENERATE_PRE_INVOICE')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        preInvoiceService.delete(id);
    }

    // ---- Export endpoints ----

    @GetMapping("/{id}/export/pdf")
    @Operation(summary = "Export pre-invoice to PDF")
    @PreAuthorize("hasAuthority('EXPORT_PRE_INVOICE')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID id) {
        PreInvoiceResponseDto inv = preInvoiceService.findById(id);
        byte[] pdf = pdfExportService.export(inv);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("pre-invoice-" + inv.getInvoiceNumber() + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/{id}/export/excel")
    @Operation(summary = "Export pre-invoice to Excel")
    @PreAuthorize("hasAuthority('EXPORT_PRE_INVOICE')")
    public ResponseEntity<byte[]> exportExcel(@PathVariable UUID id) {
        PreInvoiceResponseDto inv = preInvoiceService.findById(id);
        byte[] xlsx = excelExportService.export(inv);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("pre-invoice-" + inv.getInvoiceNumber() + ".xlsx").build());
        return ResponseEntity.ok().headers(headers).body(xlsx);
    }
}
