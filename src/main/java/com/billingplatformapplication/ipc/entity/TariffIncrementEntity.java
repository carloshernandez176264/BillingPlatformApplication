package com.billingplatformapplication.ipc.entity;

import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.rates.entity.RateEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tariff_increments")
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class TariffIncrementEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipc_rate_id", nullable = false)
    private IpcRateEntity ipcRate;

    @Column(name = "apply_year", nullable = false)
    private int applyYear;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate; // siempre 1 enero

    @Column(name = "ipc_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal ipcPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private IncrementStatus status = IncrementStatus.PENDING;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "observations", length = 500)
    private String observations;

    public enum IncrementStatus {
        PENDING,   // simulado, pendiente aprobación
        APPROVED,  // aprobado — tarifas nuevas creadas
        REJECTED   // rechazado
    }
}