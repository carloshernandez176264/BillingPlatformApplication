package com.billingplatformapplication.audit.service;

import com.billingplatformapplication.audit.entity.AuditLogEntity;
import com.billingplatformapplication.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persists an audit entry asynchronously in its own transaction.
     * Audit failures NEVER interrupt the main business operation.
     */
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, String entityId, String action,
                    String performedBy, Map<String, Object> oldValues,
                    Map<String, Object> newValues) {
        try {
            auditLogRepository.save(AuditLogEntity.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(performedBy)
                    .oldValues(sanitize(oldValues))
                    .newValues(sanitize(newValues))
                    .performedAt(Instant.now())
                    .build());
        } catch (Exception e) {
            log.error("Audit log failed entity={} action={}: {}", entityType, action, e.getMessage());
        }
    }

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogin(String email, String ipAddress, boolean success) {
        try {
            auditLogRepository.save(AuditLogEntity.builder()
                    .entityType("USER")
                    .entityId(email)
                    .action(success ? "LOGIN" : "LOGIN_FAILED")
                    .performedBy(email)
                    .ipAddress(ipAddress)
                    .performedAt(Instant.now())
                    .additionalInfo(Map.of("success", success))
                    .build());
        } catch (Exception e) {
            log.error("Audit login failed for {}: {}", email, e.getMessage());
        }
    }

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPasswordChange(String email) {
        try {
            auditLogRepository.save(AuditLogEntity.builder()
                    .entityType("USER")
                    .entityId(email)
                    .action("PASSWORD_CHANGE")
                    .performedBy(email)
                    .performedAt(Instant.now())
                    .build());
        } catch (Exception e) {
            log.error("Audit password change failed for {}: {}", email, e.getMessage());
        }
    }

    /** Remove sensitive fields before persisting. */
    private Map<String, Object> sanitize(Map<String, Object> values) {
        if (values == null) return null;
        values.remove("password");
        values.remove("passwordHash");
        values.remove("token");
        values.remove("secret");
        return values;
    }
}

