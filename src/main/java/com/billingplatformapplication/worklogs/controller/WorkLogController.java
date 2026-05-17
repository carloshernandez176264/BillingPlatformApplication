package com.billingplatformapplication.worklogs.controller;

import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.worklogs.dto.request.CreateWorkLogRequestDto;
import com.billingplatformapplication.worklogs.dto.response.WorkLogResponseDto;
import com.billingplatformapplication.worklogs.service.WorkLogService;
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
@RequestMapping("/api/v1/work-logs")
@Tag(name = "Work Logs", description = "Monthly work log registration")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_WORK_LOG')")
    public ResponseEntity<PageResponseDto<WorkLogResponseDto>> search(
            @RequestParam(required = false) UUID    clientId,
            @RequestParam(required = false) UUID    developerId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workLogService.search(clientId, developerId, year, month, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_WORK_LOG')")
    public ResponseEntity<WorkLogResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(workLogService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_WORK_LOG')")
    @Operation(summary = "Register work hours for a developer/client/period")
    public ResponseEntity<WorkLogResponseDto> create(
            @Valid @RequestBody CreateWorkLogRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workLogService.create(request));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('UPDATE_WORK_LOG')")
    @Operation(summary = "Confirm a draft work log")
    public ResponseEntity<WorkLogResponseDto> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(workLogService.confirm(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Deactivate work log — soft delete")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        workLogService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

