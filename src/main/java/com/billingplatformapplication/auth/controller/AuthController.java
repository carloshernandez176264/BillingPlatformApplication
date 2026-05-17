package com.billingplatformapplication.auth.controller;


import com.billingplatformapplication.auth.dto.request.ChangePasswordRequestDto;
import com.billingplatformapplication.auth.dto.request.LoginRequestDto;
import com.billingplatformapplication.auth.dto.request.RefreshTokenRequestDto;
import com.billingplatformapplication.auth.dto.response.AuthResponseDto;
import com.billingplatformapplication.auth.service.AuthService;
import com.billingplatformapplication.auth.token.RefreshTokenService;
import com.billingplatformapplication.security.ratelimit.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, logout, token refresh, password change")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService         authService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    @Operation(summary = "Login — returns JWT access token and refresh token")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        String ip = rateLimitService.extractIp(httpRequest);
        return ResponseEntity.ok(authService.login(request, ip));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token — rotates refresh token")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — revokes refresh token")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        refreshTokenService.revokeToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the authenticated user")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}
