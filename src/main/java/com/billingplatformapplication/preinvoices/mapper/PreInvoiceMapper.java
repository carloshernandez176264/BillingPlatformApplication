package com.billingplatformapplication.preinvoices.mapper;


import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceItemResponseDto;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceEntity;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceItemEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PreInvoiceMapper {

    @Mapping(target = "clientId",           source = "client.id")
    @Mapping(target = "clientName",         source = "client.companyName")
    @Mapping(target = "clientTaxId",        source = "client.taxId")
    @Mapping(target = "clientBillingEmail", source = "client.billingEmail")
    @Mapping(target = "currencyId",         source = "currency.id")
    @Mapping(target = "currencyCode",       source = "currency.code")
    @Mapping(target = "currencySymbol",     source = "currency.symbol")
    @Mapping(target = "status",             expression = "java(entity.getStatus().name())")
    PreInvoiceResponseDto toDto(PreInvoiceEntity entity);

    @Mapping(target = "workLogId",            source = "workLog.id")
    @Mapping(target = "developerId",          source = "developer.id")
    @Mapping(target = "developerName",        source = "developer.fullName")
    @Mapping(target = "developerProfileId",   source = "developerProfile.id")
    @Mapping(target = "developerProfileName", source = "developerProfile.name")
    PreInvoiceItemResponseDto itemToDto(PreInvoiceItemEntity item);

    List<PreInvoiceItemResponseDto> itemsToDtoList(List<PreInvoiceItemEntity> items);
}

