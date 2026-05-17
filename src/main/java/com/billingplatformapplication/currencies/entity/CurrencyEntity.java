// CurrencyEntity.java
package com.billingplatformapplication.currencies.entity;

import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CurrencyEntity extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "decimal_places", nullable = false)
    @Builder.Default
    private int decimalPlaces = 2;
}