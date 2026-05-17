package com.billingplatformapplication.billingnovelties.entity;

import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "billing_novelties",
        indexes = {
                @Index(name = "idx_novelty_worklog",     columnList = "work_log_id"),
                @Index(name = "idx_novelty_dev_period",  columnList = "developer_id,billing_year,billing_month"),
                @Index(name = "idx_novelty_status",      columnList = "approval_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BillingNoveltyEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_log_id", nullable = true)  // <- cambiar a true
    private WorkLogEntity workLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private DeveloperEntity developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Enumerated(EnumType.STRING)
    @Column(name = "novelty_type", nullable = false, length = 40)
    private NoveltyType noveltyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 10)
    private UnitType unitType;

    @Column(name = "affected_days", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal affectedDays = BigDecimal.ZERO;

    @Column(name = "affected_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal affectedHours = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "calculated_discount", precision = 19, scale = 4)
    private BigDecimal calculatedDiscount;

    @Column(name = "manual_discount_value", precision = 19, scale = 4)
    private BigDecimal manualDiscountValue;

    @Column(name = "billing_year",  nullable = false)
    private int billingYear;

    @Column(name = "billing_month", nullable = false)
    private int billingMonth;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "support_document_id")
    private UUID supportDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    public enum NoveltyType {
        SICK_LEAVE, VACATION, FAMILY_CALAMITY, PERMISSION,
        JUSTIFIED_ABSENCE, UNJUSTIFIED_ABSENCE, LICENSE,
        SUSPENSION, MANUAL_DISCOUNT, OTHER_ADJUSTMENT
    }

    public enum UnitType { DAYS, HOURS, BOTH }

    public enum ApprovalStatus { PENDING, APPROVED, REJECTED }
}