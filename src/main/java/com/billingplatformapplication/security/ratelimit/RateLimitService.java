package com.billingplatformapplication.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int      LOGIN_CAPACITY = 5;
    private static final Duration LOGIN_WINDOW   = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    public boolean tryConsumeLogin(String ip) {
        return loginBuckets.computeIfAbsent(ip, k -> newBucket()).tryConsume(1);
    }

    public String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(LOGIN_CAPACITY)
                .refillGreedy(LOGIN_CAPACITY, LOGIN_WINDOW)
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}