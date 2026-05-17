package com.billingplatformapplication.billingnovelties.repository;

import com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BillingNoveltyRepository extends JpaRepository<BillingNoveltyEntity, UUID> {

    @Query("SELECT bn FROM BillingNoveltyEntity bn " +
            "WHERE bn.workLog.id = :workLogId " +
            "AND bn.approvalStatus = 'APPROVED' " +
            "AND bn.active = true")
    List<BillingNoveltyEntity> findApprovedByWorkLogId(@Param("workLogId") UUID workLogId);

    // Nuevo método — busca por developer + client + período (sin depender del workLog)
    @Query("SELECT bn FROM BillingNoveltyEntity bn " +
            "WHERE bn.developer.id  = :developerId " +
            "AND bn.client.id       = :clientId " +
            "AND bn.billingYear     = :year " +
            "AND bn.billingMonth    = :month " +
            "AND bn.approvalStatus  = com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity.ApprovalStatus.APPROVED " +
            "AND bn.active          = true")
    List<BillingNoveltyEntity> findApprovedByDeveloperAndPeriod(
            @Param("developerId") UUID developerId,
            @Param("clientId")    UUID clientId,
            @Param("year")        int year,
            @Param("month")       int month);

    @Query("SELECT bn FROM BillingNoveltyEntity bn " +
            "LEFT JOIN FETCH bn.developer d " +
            "LEFT JOIN FETCH bn.client c " +
            "WHERE bn.workLog.id = :workLogId AND bn.active = true")
    List<BillingNoveltyEntity> findByWorkLogId(@Param("workLogId") UUID workLogId);

    @Query("SELECT bn FROM BillingNoveltyEntity bn " +
            "LEFT JOIN FETCH bn.developer " +
            "LEFT JOIN FETCH bn.client " +
            "WHERE (:workLogId     IS NULL OR bn.workLog.id     = :workLogId) " +
            "AND   (:developerId   IS NULL OR bn.developer.id   = :developerId) " +
            "AND   (:approvalStatus IS NULL OR bn.approvalStatus = :approvalStatus) " +
            "AND bn.active = true")
    Page<BillingNoveltyEntity> searchWithFilters(
            @Param("workLogId")      UUID workLogId,
            @Param("developerId")    UUID developerId,
            @Param("approvalStatus") BillingNoveltyEntity.ApprovalStatus approvalStatus,
            Pageable pageable);
}