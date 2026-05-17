package com.billingplatformapplication.audit.controller;

import com.billingplatformapplication.audit.entity.AuditLogEntity;
import com.billingplatformapplication.audit.repository.AuditLogRepository;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Immutable audit trail — read only")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Search audit logs")
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public ResponseEntity<PageResponseDto<AuditLogEntity>> search(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @ParameterObject @PageableDefault(size = 50, sort = "performedAt") Pageable pageable) {

        return ResponseEntity.ok(PageResponseDto.from(
                auditLogRepository.findWithFilters(
                        entityType, action, performedBy, from, to, pageable)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public ResponseEntity<PageResponseDto<AuditLogEntity>> findByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(PageResponseDto.from(
                auditLogRepository.findByEntityTypeAndEntityId(
                        entityType, entityId, pageable)));
    }
}
