package com.billingplatformapplication.exchangerates.repository;


import com.billingplatformapplication.exchangerates.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, UUID> {

    @Query("SELECT er FROM ExchangeRateEntity er " +
            "WHERE er.fromCurrency.id = :fromId " +
            "AND er.toCurrency.id = :toId " +
            "AND er.active = true " +
            "AND er.validFrom <= :date " +
            "AND (er.validUntil IS NULL OR er.validUntil >= :date) " +
            "ORDER BY er.validFrom DESC")
    Optional<ExchangeRateEntity> findApplicableRate(
            @Param("fromId") UUID fromId,
            @Param("toId")   UUID toId,
            @Param("date")   LocalDate date);

    boolean existsByFromCurrencyIdAndToCurrencyIdAndValidFrom(
            UUID fromCurrencyId, UUID toCurrencyId, LocalDate validFrom);
}

