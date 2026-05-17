package com.billingplatformapplication.rates.repository;


import com.billingplatformapplication.rates.entity.RateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateRepository extends JpaRepository<RateEntity, UUID> {

    Optional<RateEntity> findTopByClientIdAndDeveloperProfileIdAndStatusOrderByValidFromDesc(
            UUID clientId, UUID developerProfileId, RateEntity.RateStatus status);

    /**
     * Finds applicable rates for a client+profile on a given date.
     * Client-specific rates take priority over base rates (client IS NULL).
     * Ordered: specific first, then base; newest validFrom first.
     */
    @Query("SELECT r FROM RateEntity r " +
            "WHERE r.developerProfile.id = :profileId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.active = true " +
            "AND (r.client.id = :clientId OR r.client IS NULL) " +
            "AND r.validFrom <= :date " +
            "AND (r.validUntil IS NULL OR r.validUntil >= :date) " +
            "ORDER BY CASE WHEN r.client IS NOT NULL THEN 0 ELSE 1 END, r.validFrom DESC")
    List<RateEntity> findApplicableRates(
            @Param("clientId")  UUID      clientId,
            @Param("profileId") UUID      profileId,
            @Param("date")      LocalDate date);

    /**
     * Detects overlapping active rate for the same client/profile/currency combination.
     * Used before creating a new rate to enforce the no-overlap business rule.
     */
    @Query("SELECT COUNT(r) > 0 FROM RateEntity r " +
            "WHERE r.developerProfile.id = :profileId " +
            "AND r.currency.id = :currencyId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.active = true " +
            "AND r.id <> :excludeId " +
            "AND (:clientId IS NULL AND r.client IS NULL OR r.client.id = :clientId) " +
            "AND (r.validUntil IS NULL OR r.validUntil >= :validFrom) " +
            "AND r.validFrom <= COALESCE(:validUntil, r.validFrom)")
    boolean existsOverlapping(
            @Param("clientId")  UUID      clientId,
            @Param("profileId") UUID      profileId,
            @Param("currencyId") UUID     currencyId,
            @Param("validFrom") LocalDate validFrom,
            @Param("validUntil") LocalDate validUntil,
            @Param("excludeId") UUID      excludeId);

    @Query("SELECT r FROM RateEntity r " +
            "LEFT JOIN FETCH r.client c " +
            "LEFT JOIN FETCH r.developerProfile dp " +
            "LEFT JOIN FETCH r.currency cur " +
            "WHERE (:clientId IS NULL OR c.id = :clientId) " +
            "AND (:profileId IS NULL OR dp.id = :profileId) " +
            "AND (:status IS NULL OR r.status = :status)")
    Page<RateEntity> searchWithFilters(
            @Param("clientId")  UUID             clientId,
            @Param("profileId") UUID             profileId,
            @Param("status")    RateEntity.RateStatus status,
            Pageable pageable);
}

