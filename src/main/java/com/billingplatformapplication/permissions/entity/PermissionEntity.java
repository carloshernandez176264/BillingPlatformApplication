// PermissionEntity.java
package com.billingplatformapplication.permissions.entity;

import com.billingplatformapplication.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PermissionEntity extends AuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "module", length = 100)
    private String module;
}