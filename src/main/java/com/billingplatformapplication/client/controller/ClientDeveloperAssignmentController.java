package com.billingplatformapplication.client.controller;

import com.billingplatformapplication.client.dto.request.AssignDeveloperRequestDto;
import com.billingplatformapplication.client.dto.response.ClientDeveloperAssignmentResponseDto;
import com.billingplatformapplication.client.service.ClientDeveloperAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients/{clientId}/developers")
@RequiredArgsConstructor
public class ClientDeveloperAssignmentController {

    private final ClientDeveloperAssignmentService assignmentService;

    @GetMapping
    public List<ClientDeveloperAssignmentResponseDto> findByClient(
            @PathVariable UUID clientId) {
        return assignmentService.findByClient(clientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDeveloperAssignmentResponseDto assign(
            @PathVariable UUID clientId,
            @Valid @RequestBody AssignDeveloperRequestDto request) {
        return assignmentService.assign(clientId, request);
    }

    @DeleteMapping("/{developerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(@PathVariable UUID clientId,
                         @PathVariable UUID developerId) {
        assignmentService.unassign(clientId, developerId);
    }
}