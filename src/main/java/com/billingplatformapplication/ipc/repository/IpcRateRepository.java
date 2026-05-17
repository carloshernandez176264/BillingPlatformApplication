package com.billingplatformapplication.ipc.repository;

import com.billingplatformapplication.ipc.entity.IpcRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface IpcRateRepository extends JpaRepository<IpcRateEntity, UUID> {
    List<IpcRateEntity> findAllByOrderByYearDesc();
    boolean existsByYear(int year);
}