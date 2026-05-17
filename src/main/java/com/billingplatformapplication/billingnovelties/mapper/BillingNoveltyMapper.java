package com.billingplatformapplication.billingnovelties.mapper;

import com.billingplatformapplication.billingnovelties.dto.request.CreateBillingNoveltyRequestDto;
import com.billingplatformapplication.billingnovelties.dto.response.BillingNoveltyResponseDto;
import com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity;
import com.billingplatformapplication.shared.util.MoneyUtils;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = MoneyUtils.class)
public interface BillingNoveltyMapper {

    @Mapping(target = "developerName",    source = "developer.fullName")
    @Mapping(target = "clientName",       source = "client.companyName")
    @Mapping(target = "noveltyType",      expression = "java(entity.getNoveltyType().name())")
    @Mapping(target = "unitType",         expression = "java(entity.getUnitType().name())")
    @Mapping(target = "approvalStatus",   expression = "java(entity.getApprovalStatus().name())")
    @Mapping(target = "effectiveDiscount",
            expression = "java(MoneyUtils.isPositive(entity.getManualDiscountValue()) " +
                    "? entity.getManualDiscountValue() : entity.getCalculatedDiscount())")
    BillingNoveltyResponseDto toDto(BillingNoveltyEntity entity);

    List<BillingNoveltyResponseDto> toDtoList(List<BillingNoveltyEntity> entities);

    @Mapping(target = "id",                 ignore = true)
    @Mapping(target = "workLog",            ignore = true)
    @Mapping(target = "developer",          ignore = true)
    @Mapping(target = "client",             ignore = true)
    @Mapping(target = "noveltyType",        expression = "java(com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity.NoveltyType.valueOf(dto.noveltyType().toUpperCase()))")
    @Mapping(target = "unitType",           expression = "java(com.billingplatformapplication.billingnovelties.entity.BillingNoveltyEntity.UnitType.valueOf(dto.unitType().toUpperCase()))")
    @Mapping(target = "calculatedDiscount", ignore = true)
    @Mapping(target = "approvalStatus",     constant = "PENDING")
    @Mapping(target = "approvedBy",         ignore = true)
    @Mapping(target = "rejectionReason",    ignore = true)
    @Mapping(target = "supportDocumentId",  ignore = true)
    @Mapping(target = "active",             ignore = true)
    @Mapping(target = "createdAt",          ignore = true)
    @Mapping(target = "updatedAt",          ignore = true)
    @Mapping(target = "createdBy",          ignore = true)
    @Mapping(target = "updatedBy",          ignore = true)
    BillingNoveltyEntity toEntity(CreateBillingNoveltyRequestDto dto);
}