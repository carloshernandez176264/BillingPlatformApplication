package com.billingplatformapplication.developerprofiles.entity;

import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "developer_profiles")
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class DeveloperProfileEntity extends AuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "level", length = 30)
    private String level;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_skills", columnDefinition = "TEXT")
    private String baseSkills;

    // Tarifa base interna por perfil (precio de lista)
    @Column(name = "base_monthly_rate", precision = 19, scale = 4)
    private BigDecimal baseMonthlyRate;
}