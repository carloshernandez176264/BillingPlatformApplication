package com.billingplatformapplication.preinvoices.repository;



import com.billingplatformapplication.preinvoices.entity.PreInvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreInvoiceRepository extends JpaRepository<PreInvoiceEntity, UUID> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT pi FROM PreInvoiceEntity pi " +
            "LEFT JOIN FETCH pi.client c " +
            "LEFT JOIN FETCH pi.currency cur " +
            "LEFT JOIN FETCH pi.items " +
            "WHERE pi.id = :id")
    Optional<PreInvoiceEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT pi FROM PreInvoiceEntity pi " +
            "LEFT JOIN FETCH pi.client " +
            "LEFT JOIN FETCH pi.currency " +
            "WHERE (:clientId IS NULL OR pi.client.id = :clientId) " +
            "AND   (:status   IS NULL OR pi.status    = :status) " +
            "AND   (:year     IS NULL OR pi.billingYear  = :year) " +
            "AND   (:month    IS NULL OR pi.billingMonth = :month) " +
            "AND pi.active = true")
    Page<PreInvoiceEntity> searchWithFilters(
            @Param("clientId") UUID clientId,
            @Param("status")   PreInvoiceEntity.PreInvoiceStatus status,
            @Param("year")     Integer year,
            @Param("month")    Integer month,
            Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(pi.invoiceNumber, 4) AS int)), 0) " +
            "FROM PreInvoiceEntity pi WHERE pi.invoiceNumber LIKE 'PRE%'")
    Integer findMaxInvoiceSequence();
}

