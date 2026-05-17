package com.billingplatformapplication.audit.repository;


import com.billingplatformapplication.audit.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    Page<AuditLogEntity> findByEntityTypeAndEntityId(
            String entityType, String entityId, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:action     IS NULL OR a.action     = :action)     AND " +
            "(:performedBy IS NULL OR a.performedBy = :performedBy) AND " +
            "(:from IS NULL OR a.performedAt >= :from) AND " +
            "(:to   IS NULL OR a.performedAt <= :to)")
    Page<AuditLogEntity> findWithFilters(
            @Param("entityType")  String  entityType,
            @Param("action")      String  action,
            @Param("performedBy") String  performedBy,
            @Param("from")        Instant from,
            @Param("to")          Instant to,
            Pageable pageable);
}
