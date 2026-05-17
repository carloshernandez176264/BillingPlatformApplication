package com.billingplatformapplication.client.repository;

import com.billingplatformapplication.client.entity.ClientDeveloperAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientDeveloperAssignmentRepository
        extends JpaRepository<ClientDeveloperAssignmentEntity, UUID> {

    List<ClientDeveloperAssignmentEntity> findByClientIdAndActiveTrue(UUID clientId);

    boolean existsByClientIdAndDeveloperIdAndActiveTrue(UUID clientId, UUID developerId);

    @Query("SELECT a FROM ClientDeveloperAssignmentEntity a " +
            "LEFT JOIN FETCH a.developer d " +
            "LEFT JOIN FETCH d.profile " +
            "WHERE a.client.id = :clientId AND a.active = true " +
            "ORDER BY d.fullName")
    List<ClientDeveloperAssignmentEntity> findActiveByClientWithDeveloper(@Param("clientId") UUID clientId);

    @Query("SELECT a FROM ClientDeveloperAssignmentEntity a " +
            "WHERE a.client.id = :clientId AND a.developer.id = :developerId")
    Optional<ClientDeveloperAssignmentEntity> findByClientAndDeveloper(
            @Param("clientId")   UUID clientId,
            @Param("developerId") UUID developerId);
}
