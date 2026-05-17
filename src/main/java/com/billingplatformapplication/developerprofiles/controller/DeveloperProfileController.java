package com.billingplatformapplication.developerprofiles.controller;


import com.billingplatformapplication.developerprofiles.dto.request.CreateDeveloperProfileRequestDto;
import com.billingplatformapplication.developerprofiles.dto.response.DeveloperProfileResponseDto;
import com.billingplatformapplication.developerprofiles.service.DeveloperProfileService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/developer-profiles")
@Tag(name = "Developer Profiles", description = "Developer profile management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DeveloperProfileController {

    private final DeveloperProfileService profileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active profiles")
    public ResponseEntity<List<DeveloperProfileResponseDto>> findAllActive() {
        return ResponseEntity.ok(profileService.findAllActive());
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponseDto<DeveloperProfileResponseDto>> search(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(profileService.search(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeveloperProfileResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DeveloperProfileResponseDto> create(
            @Valid @RequestBody CreateDeveloperProfileRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DeveloperProfileResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDeveloperProfileRequestDto request) {
        return ResponseEntity.ok(profileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate profile — soft delete")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        profileService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
