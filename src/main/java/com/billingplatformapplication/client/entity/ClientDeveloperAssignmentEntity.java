package com.billingplatformapplication.client.entity;

import com.billingplatformapplication.developers.entity.DeveloperEntity;

import com.billingplatformapplication.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "client_developer_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "developer_id"}))
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class ClientDeveloperAssignmentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private DeveloperEntity developer;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String notes;
}
