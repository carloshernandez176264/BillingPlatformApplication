package com.billingplatformapplication.auth.service;


import com.billingplatformapplication.audit.service.AuditService;
import com.billingplatformapplication.auth.dto.request.ChangePasswordRequestDto;
import com.billingplatformapplication.auth.dto.request.LoginRequestDto;
import com.billingplatformapplication.auth.dto.response.AuthResponseDto;
import com.billingplatformapplication.auth.token.RefreshTokenService;
import com.billingplatformapplication.security.audit.SecurityAuditLogger;
import com.billingplatformapplication.security.jwt.JwtTokenProvider;
import com.billingplatformapplication.shared.exception.BusinessException;
import com.billingplatformapplication.users.entity.UserEntity;
import com.billingplatformapplication.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecurityAuditLogger securityAuditLogger;

    @Transactional
    public AuthResponseDto login(LoginRequestDto request, String ip) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        validateState(user);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            // Success — reset counters
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            securityAuditLogger.logLoginSuccess(user.getEmail(), ip);
            auditService.logLogin(user.getEmail(), ip, true);

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900)
                    .mustChangePassword(user.isMustChangePassword())
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedAttempt(user, ip);
            throw new BadCredentialsException("Invalid credentials"); // generic message
        }
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequestDto request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("New password and confirmation do not match");
        }
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessException("New password must differ from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Revoke all active refresh tokens — force re-login
        refreshTokenService.revokeAllUserTokens(user.getId());
        securityAuditLogger.logPasswordChange(email);
        auditService.logPasswordChange(email);
    }

    // ----------------------------------------------------------------

    private void validateState(UserEntity user) {
        if (user.isLocked()) {
            throw new LockedException("Account is locked due to multiple failed attempts");
        }
        if (user.getStatus() != UserEntity.UserStatus.ACTIVE || !user.isActive()) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private void handleFailedAttempt(UserEntity user, String ip) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLocked(true);
            securityAuditLogger.logAccountLocked(user.getEmail(), ip);
            log.warn("Account locked: {} after {} attempts", user.getEmail(), attempts);
        }

        userRepository.save(user);
        securityAuditLogger.logLoginFailure(user.getEmail(), ip, attempts);
        auditService.logLogin(user.getEmail(), ip, false);
    }
}
