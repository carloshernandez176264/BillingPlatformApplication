package com.billingplatformapplication.ipc.entity;

import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "ipc_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = "year"))
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class IpcRateEntity extends AuditableEntity {

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "ipc_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal ipcPercentage;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "source", length = 100)
    private String source; // Ej: "DANE - Colombia"
}