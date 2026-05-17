package com.billingplatformapplication.worklogs.repository;

import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLogEntity, UUID> {

    boolean existsByClientIdAndDeveloperIdAndDeveloperProfileIdAndBillingYearAndBillingMonth(
            UUID clientId, UUID developerId, UUID developerProfileId, int year, int month);

    // Busca work_log existente por developer + client + período
    @Query("SELECT wl FROM WorkLogEntity wl " +
            "WHERE wl.client.id    = :clientId " +
            "AND wl.developer.id   = :developerId " +
            "AND wl.billingYear    = :year " +
            "AND wl.billingMonth   = :month " +
            "AND wl.active         = true")
    Optional<WorkLogEntity> findByClientDeveloperAndPeriod(
            @Param("clientId")    UUID clientId,
            @Param("developerId") UUID developerId,
            @Param("year")        int  year,
            @Param("month")       int  month);

    @Query("SELECT wl FROM WorkLogEntity wl " +
            "LEFT JOIN FETCH wl.client c " +
            "LEFT JOIN FETCH wl.developer d " +
            "LEFT JOIN FETCH wl.developerProfile dp " +
            "LEFT JOIN FETCH wl.appliedRate r " +
            "WHERE c.id = :clientId " +
            "AND wl.billingYear  = :year " +
            "AND wl.billingMonth = :month " +
            "AND wl.active       = true")
    List<WorkLogEntity> findByClientIdAndPeriod(
            @Param("clientId") UUID clientId,
            @Param("year")     int  year,
            @Param("month")    int  month);

    @Query("SELECT wl FROM WorkLogEntity wl " +
            "LEFT JOIN FETCH wl.client " +
            "LEFT JOIN FETCH wl.developer " +
            "LEFT JOIN FETCH wl.developerProfile " +
            "WHERE (:clientId    IS NULL OR wl.client.id    = :clientId) " +
            "AND   (:developerId IS NULL OR wl.developer.id = :developerId) " +
            "AND   (:year  IS NULL OR wl.billingYear  = :year) " +
            "AND   (:month IS NULL OR wl.billingMonth = :month) " +
            "AND wl.active = true")
    Page<WorkLogEntity> searchWithFilters(
            @Param("clientId")    UUID    clientId,
            @Param("developerId") UUID    developerId,
            @Param("year")        Integer year,
            @Param("month")       Integer month,
            Pageable pageable);
}