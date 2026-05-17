package com.billingplatformapplication.security.jwt;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtTokenProvider jwtTokenProvider;

    public boolean isValid(String token, UserDetails userDetails) {
        try {
            String username = jwtTokenProvider.extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !jwtTokenProvider.isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}