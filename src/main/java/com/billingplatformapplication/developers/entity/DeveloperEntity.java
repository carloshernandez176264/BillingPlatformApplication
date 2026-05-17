package com.billingplatformapplication.developers.entity;

import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "developers",
        indexes = {
                @Index(name = "idx_developers_document", columnList = "document_id", unique = true),
                @Index(name = "idx_developers_profile",  columnList = "profile_id"),
                @Index(name = "idx_developers_status",   columnList = "status")
        }
)
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class DeveloperEntity extends AuditableEntity {

    @Column(name = "document_id", nullable = false, unique = true, length = 30)
    private String documentId;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "email", length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private DeveloperProfileEntity profile;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "assignment_mode", length = 50)
    private String assignmentMode;

    // Salario base — CONFIDENCIAL — solo visible para ADMIN y FINANCE
    @Column(name = "base_salary", precision = 19, scale = 4)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DeveloperStatus status = DeveloperStatus.ACTIVE;

    public enum DeveloperStatus { ACTIVE, INACTIVE, ON_LEAVE }
}