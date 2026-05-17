package com.billingplatformapplication.client.repository;


import com.billingplatformapplication.client.entity.ClientEntity;
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
public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {

    boolean existsByTaxId(String taxId);

    List<ClientEntity> findByActiveTrue();

    @Query("SELECT c FROM ClientEntity c " +
            "LEFT JOIN FETCH c.primaryCurrency " +
            "WHERE c.id = :id")
    Optional<ClientEntity> findByIdWithCurrency(@Param("id") UUID id);

    @Query("SELECT c FROM ClientEntity c " +
            "LEFT JOIN FETCH c.primaryCurrency " +
            "WHERE (:search IS NULL " +
            "  OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "  OR LOWER(c.taxId)       LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<ClientEntity> searchWithFilters(
            @Param("search") String search,
            @Param("status") ClientEntity.ClientStatus status,
            Pageable pageable);
}

