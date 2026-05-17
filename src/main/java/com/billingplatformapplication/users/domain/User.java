package com.billingplatformapplication.users.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Domain model — pure business object, no JPA annotations.
 * Used in use cases and services to keep domain logic
 * independent of the persistence layer.
 */
@Getter
@Builder
public class User {
    private UUID    id;
    private String  email;
    private String  fullName;
    private String  status;
    private boolean locked;
    private boolean mustChangePassword;
    private Set<String> roles;
    private Instant createdAt;
    private Instant lastLoginAt;
}

