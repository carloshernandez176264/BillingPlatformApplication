package com.billingplatformapplication.client.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/** Domain model — JPA-free. */
@Getter
@Builder
public class Client {
    private UUID id;
    private String taxId;
    private String companyName;
    private String tradeName;
    private String country;
    private String billingEmail;
    private String status;
    private String primaryCurrencyCode;
}
