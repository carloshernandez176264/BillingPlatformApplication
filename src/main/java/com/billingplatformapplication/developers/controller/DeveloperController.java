package com.billingplatformapplication.developers.controller;



import com.billingplatformapplication.developers.dto.request.CreateDeveloperRequestDto;
import com.billingplatformapplication.developers.dto.response.DeveloperResponseDto;
import com.billingplatformapplication.developers.service.DeveloperService;
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
@RequestMapping("/api/v1/developers")
@Tag(name = "Developers", description = "Developer / billable resource management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CLIENT') or hasRole('MANAGER')")
    public ResponseEntity<PageResponseDto<DeveloperResponseDto>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID   profileId,
            @ParameterObject @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return ResponseEntity.ok(developerService.search(search, status, profileId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeveloperResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(developerService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Register a new developer / billable resource")
    public ResponseEntity<DeveloperResponseDto> create(
            @Valid @RequestBody CreateDeveloperRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(developerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DeveloperResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDeveloperRequestDto request) {
        return ResponseEntity.ok(developerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate developer — soft delete")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        developerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
