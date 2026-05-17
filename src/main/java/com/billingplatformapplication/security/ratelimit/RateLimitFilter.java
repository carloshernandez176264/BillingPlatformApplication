package com.billingplatformapplication.security.ratelimit;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (request.getRequestURI().endsWith("/api/v1/auth/login")) {
            String ip = rateLimitService.extractIp(request);
            if (!rateLimitService.tryConsumeLogin(ip)) {
                log.warn("Rate limit exceeded: login from ip={}", ip);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\"," +
                                "\"message\":\"Too many login attempts. Try again later.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
