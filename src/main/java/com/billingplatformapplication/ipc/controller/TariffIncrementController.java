package com.billingplatformapplication.ipc.controller;

import com.billingplatformapplication.ipc.dto.*;
import com.billingplatformapplication.ipc.dto.request.ApproveIncrementRequestDto;
import com.billingplatformapplication.ipc.dto.request.CreateIpcRateRequestDto;
import com.billingplatformapplication.ipc.dto.response.IpcRateResponseDto;
import com.billingplatformapplication.ipc.entity.TariffIncrementEntity;
import com.billingplatformapplication.ipc.service.TariffIncrementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tariff-increments")
@RequiredArgsConstructor
public class TariffIncrementController {

    private final TariffIncrementService service;

    // IPC
    @GetMapping("/ipc")
    public List<IpcRateResponseDto> findAllIpc() {
        return service.findAllIpc();
    }

    @PostMapping("/ipc")
    @ResponseStatus(HttpStatus.CREATED)
    public IpcRateResponseDto createIpc(@Valid @RequestBody CreateIpcRateRequestDto request) {
        return service.createIpc(request);
    }

    // Simulación
    @GetMapping("/simulate")
    public TariffIncrementSimulationDto simulate(
            @RequestParam UUID clientId,
            @RequestParam UUID ipcRateId) {
        return service.simulate(clientId, ipcRateId);
    }

    // Aprobar
    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.CREATED)
    public TariffIncrementEntity approve(
            @Valid @RequestBody ApproveIncrementRequestDto request) {
        return service.approve(request);
    }

    // Historial
    @GetMapping("/history")
    public List<TariffIncrementEntity> history(@RequestParam UUID clientId) {
        return service.findHistory(clientId);
    }
}