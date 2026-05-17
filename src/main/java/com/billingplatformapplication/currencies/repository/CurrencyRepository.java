package com.billingplatformapplication.currencies.repository;


import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, UUID> {
    Optional<CurrencyEntity> findByCode(String code);
    boolean existsByCode(String code);
    List<CurrencyEntity> findByActiveTrue();
}
