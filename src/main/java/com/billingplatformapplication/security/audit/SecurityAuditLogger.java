package com.billingplatformapplication.security.audit;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityAuditLogger {

    private static final org.slf4j.Logger SECURITY =
            LoggerFactory.getLogger("SECURITY_AUDIT");

    public void logLoginSuccess(String email, String ip) {
        SECURITY.info("LOGIN_SUCCESS user={} ip={}", email, ip);
    }

    public void logLoginFailure(String email, String ip, int attempt) {
        SECURITY.warn("LOGIN_FAILURE user={} ip={} attempt={}", email, ip, attempt);
    }

    public void logAccountLocked(String email, String ip) {
        SECURITY.warn("ACCOUNT_LOCKED user={} ip={}", email, ip);
    }

    public void logPasswordChange(String email) {
        SECURITY.info("PASSWORD_CHANGE user={}", email);
    }

    public void logTokenRevoked(String email) {
        SECURITY.info("TOKEN_REVOKED user={}", email);
    }

    public void logAccessDenied(String email, String uri) {
        SECURITY.warn("ACCESS_DENIED user={} uri={}", email, uri);
    }
}

