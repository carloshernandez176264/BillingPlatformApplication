// ExchangeRateEntity.java
package com.billingplatformapplication.exchangerates.entity;

import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "exchange_rates",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_exchange_rate_period",
                columnNames = {"from_currency_id", "to_currency_id", "valid_from"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ExchangeRateEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    private CurrencyEntity fromCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    private CurrencyEntity toCurrency;

    @Column(name = "rate", precision = 19, scale = 8, nullable = false)
    private BigDecimal rate;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;
}