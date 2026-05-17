package com.billingplatformapplication.client.controller;


import com.billingplatformapplication.client.dto.request.CreateClientRequestDto;
import com.billingplatformapplication.client.dto.request.UpdateClientRequestDto;
import com.billingplatformapplication.client.dto.response.ClientResponseDto;
import com.billingplatformapplication.client.service.ClientService;
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
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Client management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CLIENT')")
    @Operation(summary = "List clients with pagination, search and status filter")
    public ResponseEntity<PageResponseDto<ClientResponseDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20, sort = "companyName") Pageable pageable) {
        return ResponseEntity.ok(clientService.findAll(search, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_CLIENT')")
    public ResponseEntity<ClientResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CLIENT')")
    @Operation(summary = "Create new client")
    public ResponseEntity<ClientResponseDto> create(
            @Valid @RequestBody CreateClientRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_CLIENT')")
    @Operation(summary = "Update client")
    public ResponseEntity<ClientResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequestDto request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_CLIENT')")
    @Operation(summary = "Deactivate client — soft delete")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        clientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
