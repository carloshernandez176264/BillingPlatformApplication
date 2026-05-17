// AuditableEntity.java
package com.billingplatformapplication.shared.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {
}