package com.billingplatformapplication.billingnovelties.controller;

import com.billingplatformapplication.billingnovelties.dto.request.CreateBillingNoveltyRequestDto;
import com.billingplatformapplication.billingnovelties.dto.response.BillingNoveltyResponseDto;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.billingnovelties.service.BillingNoveltyService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing-novelties")
@Tag(name = "Billing Novelties", description = "Billing novelty (discount) management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BillingNoveltyController {

    private final BillingNoveltyService noveltyService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_BILLING_NOVELTY')")
    public ResponseEntity<PageResponseDto<BillingNoveltyResponseDto>> search(
            @RequestParam(required = false) UUID   workLogId,
            @RequestParam(required = false) UUID   developerId,
            @RequestParam(required = false) String approvalStatus,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                noveltyService.search(workLogId, developerId, approvalStatus, pageable));
    }

    @GetMapping("/by-work-log/{workLogId}")
    @PreAuthorize("hasAuthority('READ_BILLING_NOVELTY')")
    public ResponseEntity<List<BillingNoveltyResponseDto>> findByWorkLog(
            @PathVariable UUID workLogId) {
        return ResponseEntity.ok(noveltyService.findByWorkLog(workLogId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_BILLING_NOVELTY')")
    public ResponseEntity<BillingNoveltyResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(noveltyService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_BILLING_NOVELTY')")
    @Operation(summary = "Create billing novelty — discount auto-calculated by backend")
    public ResponseEntity<BillingNoveltyResponseDto> create(
            @Valid @RequestBody CreateBillingNoveltyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noveltyService.create(request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UPDATE_BILLING_NOVELTY')")
    @Operation(summary = "Approve a pending novelty")
    public ResponseEntity<BillingNoveltyResponseDto> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noveltyService.approve(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UPDATE_BILLING_NOVELTY')")
    @Operation(summary = "Reject a pending novelty with reason")
    public ResponseEntity<BillingNoveltyResponseDto> reject(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(noveltyService.reject(id, reason));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        noveltyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
