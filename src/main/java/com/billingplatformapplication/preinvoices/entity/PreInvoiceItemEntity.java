package com.billingplatformapplication.preinvoices.entity;

import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "pre_invoice_items",
        indexes = @Index(name = "idx_pi_items_invoice", columnList = "pre_invoice_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreInvoiceItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_invoice_id", nullable = false)
    private PreInvoiceEntity preInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_log_id", nullable = true)
    private WorkLogEntity workLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private DeveloperEntity developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_profile_id", nullable = false)
    private DeveloperProfileEntity developerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_id")
    private RateEntity rate;

    @Column(name = "rate_type",  length = 20)  private String rateType;
    @Column(name = "rate_value", precision = 19, scale = 4) private BigDecimal rateValue;

    @Column(name = "billed_hours", precision = 6,  scale = 2) private BigDecimal billedHours;
    @Column(name = "billed_days",  precision = 4,  scale = 1) private BigDecimal billedDays;

    @Column(name = "gross_amount",    precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal grossAmount    = BigDecimal.ZERO;
    @Column(name = "novelty_discount",precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal noveltyDiscount = BigDecimal.ZERO;
    @Column(name = "other_discount",  precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal otherDiscount   = BigDecimal.ZERO;
    @Column(name = "net_amount",      precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal netAmount       = BigDecimal.ZERO;

    @Column(name = "line_description", length = 500) private String lineDescription;
    @Column(name = "sort_order", nullable = false)   @Builder.Default private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

