package com.billingplatformapplication.users.controller;

import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.users.dto.request.CreateUserRequestDto;
import com.billingplatformapplication.users.dto.request.UpdateUserRequestDto;
import com.billingplatformapplication.users.dto.response.UserResponseDto;
import com.billingplatformapplication.users.service.UserService;
import com.billingplatformapplication.users.usecase.CreateUserUseCase;
import com.billingplatformapplication.users.usecase.UpdateUserUseCase;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Platform user management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "List users with pagination and search")
    public ResponseEntity<PageResponseDto<UserResponseDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20, sort = "email") Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(search, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Create new user")
    public ResponseEntity<UserResponseDto> create(
            @Valid @RequestBody CreateUserRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createUserUseCase.execute(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(updateUserUseCase.execute(id, request));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Unlock a blocked user account")
    public ResponseEntity<Void> unlock(@PathVariable UUID id) {
        userService.unlock(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Deactivate user — soft delete")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
