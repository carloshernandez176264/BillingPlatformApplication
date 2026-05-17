package com.billingplatformapplication.client.entity;

import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "clients",
        indexes = {
                @Index(name = "idx_clients_tax_id", columnList = "tax_id", unique = true),
                @Index(name = "idx_clients_status", columnList = "status"),
                @Index(name = "idx_clients_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ClientEntity extends AuditableEntity {

    @Column(name = "tax_id", nullable = false, unique = true, length = 30)
    private String taxId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Column(name = "country", nullable = false, length = 60)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "billing_email", nullable = false, length = 150)
    private String billingEmail;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_currency_id", nullable = false)
    private CurrencyEntity primaryCurrency;

    @Column(name = "tax_regime", length = 100)
    private String taxRegime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ClientStatus status = ClientStatus.ACTIVE;

    public enum ClientStatus { ACTIVE, INACTIVE, SUSPENDED }
}