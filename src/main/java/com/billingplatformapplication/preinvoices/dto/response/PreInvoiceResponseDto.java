package com.billingplatformapplication.preinvoices.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class PreInvoiceResponseDto {
    private UUID       id;
    private String     invoiceNumber;
    private UUID       clientId;
    private String     clientName;
    private String     clientTaxId;
    private String     clientBillingEmail;
    private UUID       currencyId;
    private String     currencyCode;
    private String     currencySymbol;
    private int        billingYear;
    private int        billingMonth;
    private String     periodDescription;
    private List<PreInvoiceItemResponseDto> items;
    private BigDecimal subtotal;
    private BigDecimal totalNoveltyDiscounts;
    private BigDecimal totalOtherDiscounts;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDate  generationDate;
    private LocalDate  dueDate;
    private String     status;
    private String     observations;
    private String     rejectionReason;
    private int        version;
    private Instant    createdAt;
    private String     createdBy;
    private Instant    updatedAt;
    private String     updatedBy;
}
