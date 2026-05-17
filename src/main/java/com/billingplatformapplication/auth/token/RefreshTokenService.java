package com.billingplatformapplication.auth.token;


import com.billingplatformapplication.auth.dto.request.RefreshTokenRequestDto;
import com.billingplatformapplication.auth.dto.response.AuthResponseDto;
import com.billingplatformapplication.security.jwt.JwtTokenProvider;
import com.billingplatformapplication.security.userdetails.UserDetailsServiceImpl;
import com.billingplatformapplication.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository  refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @Transactional
    public String createRefreshToken(UUID userId) {
        // Raw random token — only the SHA-256 hash is persisted
        String rawToken  = UUID.randomUUID() + UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        refreshTokenRepository.save(token);
        return rawToken;
    }

    @Transactional
    public AuthResponseDto refreshAccessToken(RefreshTokenRequestDto request) {
        String tokenHash = hash(request.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (!stored.isValid()) {
            throw new BusinessException("Refresh token is expired or revoked");
        }

        // Token rotation — revoke old, issue new
        stored.setRevoked(true);
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        UserDetails userDetails = userDetailsService.loadUserByUserId(stored.getUserId());
        String newAccessToken  = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = createRefreshToken(stored.getUserId());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .mustChangePassword(false)
                .build();
    }

    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = hash(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(t -> {
            t.setRevoked(true);
            t.setRevokedAt(Instant.now());
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Scheduled(fixedDelay = 3_600_000) // every hour
    @Transactional
    public void purgeExpiredTokens() {
        log.debug("Purging expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens();
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
