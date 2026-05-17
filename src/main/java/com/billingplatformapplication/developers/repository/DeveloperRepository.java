package com.billingplatformapplication.developers.repository;

import com.billingplatformapplication.developers.entity.DeveloperEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeveloperRepository extends JpaRepository<DeveloperEntity, UUID> {

    boolean existsByDocumentId(String documentId);

    List<DeveloperEntity> findByActiveTrueAndStatus(DeveloperEntity.DeveloperStatus status);

    @Query(value = "SELECT d FROM DeveloperEntity d " +
            "LEFT JOIN FETCH d.profile " +
            "WHERE d.active = true " +
            "AND (:search IS NULL " +
            "  OR LOWER(d.fullName)   LIKE :searchPattern " +
            "  OR LOWER(d.documentId) LIKE :searchPattern) " +
            "AND (:status IS NULL OR d.status = :status) " +
            "AND (:profileId IS NULL OR d.profile.id = :profileId)",
            countQuery = "SELECT COUNT(d) FROM DeveloperEntity d " +
                    "LEFT JOIN d.profile p " +
                    "WHERE d.active = true " +
                    "AND (:search IS NULL " +
                    "  OR LOWER(d.fullName)   LIKE :searchPattern " +
                    "  OR LOWER(d.documentId) LIKE :searchPattern) " +
                    "AND (:status IS NULL OR d.status = :status) " +
                    "AND (:profileId IS NULL OR d.profile.id = :profileId)")
    Page<DeveloperEntity> searchWithFilters(
            @Param("search")        String search,
            @Param("searchPattern") String searchPattern,
            @Param("status")        DeveloperEntity.DeveloperStatus status,
            @Param("profileId")     UUID profileId,
            Pageable pageable);
}