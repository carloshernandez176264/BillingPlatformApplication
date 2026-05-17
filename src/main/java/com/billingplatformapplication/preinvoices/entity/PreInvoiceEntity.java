// PreInvoiceEntity.java
package com.billingplatformapplication.preinvoices.entity;

import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "pre_invoices",
        indexes = {
                @Index(name = "idx_pi_client", columnList = "client_id"),
                @Index(name = "idx_pi_number", columnList = "invoice_number", unique = true),
                @Index(name = "idx_pi_period", columnList = "billing_year,billing_month"),
                @Index(name = "idx_pi_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PreInvoiceEntity extends AuditableEntity {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    @Column(name = "billing_year",  nullable = false) private int billingYear;
    @Column(name = "billing_month", nullable = false) private int billingMonth;
    @Column(name = "period_description", length = 100) private String periodDescription;

    @Column(name = "subtotal",                precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal subtotal              = BigDecimal.ZERO;
    @Column(name = "total_novelty_discounts", precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal totalNoveltyDiscounts = BigDecimal.ZERO;
    @Column(name = "total_other_discounts",   precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal totalOtherDiscounts   = BigDecimal.ZERO;
    @Column(name = "taxable_amount",          precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal taxableAmount         = BigDecimal.ZERO;
    @Column(name = "tax_amount",              precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal taxAmount             = BigDecimal.ZERO;
    @Column(name = "total_amount",            precision = 19, scale = 4, nullable = false) @Builder.Default private BigDecimal totalAmount           = BigDecimal.ZERO;

    @Column(name = "generation_date", nullable = false) private LocalDate generationDate;
    @Column(name = "due_date") private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PreInvoiceStatus status = PreInvoiceStatus.DRAFT;

    @Column(name = "observations",     columnDefinition = "TEXT") private String observations;
    @Column(name = "rejection_reason", columnDefinition = "TEXT") private String rejectionReason;

    @Column(name = "version", nullable = false) @Builder.Default private int version = 1;

    @OneToMany(mappedBy = "preInvoice", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PreInvoiceItemEntity> items = new ArrayList<>();

    public boolean isEditable() {
        return status == PreInvoiceStatus.DRAFT || status == PreInvoiceStatus.GENERATED;
    }

    public enum PreInvoiceStatus {
        DRAFT, GENERATED, SENT_TO_CLIENT, APPROVED, REJECTED, CANCELLED, INVOICED
    }
}