package com.billingplatformapplication.ipc.repository;

import com.billingplatformapplication.ipc.entity.TariffIncrementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TariffIncrementRepository extends JpaRepository<TariffIncrementEntity, UUID> {
    List<TariffIncrementEntity> findByClientIdOrderByApplyYearDesc(UUID clientId);
    boolean existsByClientIdAndApplyYearAndStatus(
            UUID clientId, int applyYear,
            TariffIncrementEntity.IncrementStatus status);
}