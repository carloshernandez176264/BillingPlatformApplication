// WorkLogEntity.java
package com.billingplatformapplication.worklogs.entity;

import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(
        name = "work_logs",
        indexes = {
                @Index(name = "idx_wl_client_period",
                        columnList = "client_id,billing_year,billing_month"),
                @Index(name = "idx_wl_developer", columnList = "developer_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_work_log_period",
                columnNames = {"client_id", "developer_id", "developer_profile_id",
                        "billing_year", "billing_month"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class WorkLogEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private DeveloperEntity developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_profile_id", nullable = false)
    private DeveloperProfileEntity developerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_rate_id")
    private RateEntity appliedRate;

    @Column(name = "billing_year",  nullable = false)
    private int billingYear;

    @Column(name = "billing_month", nullable = false)
    private int billingMonth;

    @Column(name = "expected_working_days", nullable = false)
    private int expectedWorkingDays;

    @Column(name = "expected_working_hours", precision = 6, scale = 2, nullable = false)
    private BigDecimal expectedWorkingHours;

    @Column(name = "actual_worked_hours", precision = 6, scale = 2, nullable = false)
    private BigDecimal actualWorkedHours;

    @Column(name = "billable_amount", precision = 19, scale = 4)
    private BigDecimal billableAmount;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WorkLogStatus status = WorkLogStatus.DRAFT;

    public enum WorkLogStatus { DRAFT, CONFIRMED, BILLED }
}