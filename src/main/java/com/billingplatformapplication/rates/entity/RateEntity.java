package com.billingplatformapplication.rates.entity;

import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "rates",
        indexes = {
                @Index(name = "idx_rates_client_profile",
                        columnList = "client_id,developer_profile_id"),
                @Index(name = "idx_rates_validity",
                        columnList = "valid_from,valid_until"),
                @Index(name = "idx_rates_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RateEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_profile_id", nullable = false)
    private DeveloperProfileEntity developerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private RateType rateType;

    @Column(name = "monthly_rate", precision = 19, scale = 4)
    private BigDecimal monthlyRate;

    @Column(name = "daily_rate", precision = 19, scale = 4)
    private BigDecimal dailyRate;

    @Column(name = "hourly_rate", precision = 19, scale = 4)
    private BigDecimal hourlyRate;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "includes_tax", nullable = false)
    @Builder.Default
    private boolean includesTax = false;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "working_hours_per_day", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal workingHoursPerDay = new BigDecimal("8.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RateStatus status = RateStatus.ACTIVE;

    @Column(name = "commercial_notes", columnDefinition = "TEXT")
    private String commercialNotes;

    public enum RateType  { MONTHLY, DAILY, HOURLY }
    public enum RateStatus { ACTIVE, INACTIVE, EXPIRED }
}