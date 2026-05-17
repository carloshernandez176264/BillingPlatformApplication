package com.billingplatformapplication.roles.controller;

import com.billingplatformapplication.roles.dto.RoleResponseDto;
import com.billingplatformapplication.roles.service.RoleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Gestión de roles del sistema")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<RoleResponseDto> findAll() {
        return roleService.findAll().stream()
                .map(r -> new RoleResponseDto(
                        r.getId(),
                        r.getName(),
                        r.getDescription() != null ? r.getDescription() : "",
                        r.isActive()
                ))
                .toList();
    }
}