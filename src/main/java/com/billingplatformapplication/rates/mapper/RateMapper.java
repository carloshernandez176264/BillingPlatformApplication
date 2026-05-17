package com.billingplatformapplication.rates.mapper;

import com.billingplatformapplication.rates.dto.request.CreateRateRequestDto;
import com.billingplatformapplication.rates.dto.response.RateResponseDto;
import com.billingplatformapplication.rates.entity.RateEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RateMapper {

    @Mapping(target = "clientId",             source = "client.id")
    @Mapping(target = "clientName",           source = "client.companyName")
    @Mapping(target = "developerProfileId",   source = "developerProfile.id")
    @Mapping(target = "developerProfileName", source = "developerProfile.name")
    @Mapping(target = "currencyId",           source = "currency.id")
    @Mapping(target = "currencyCode",         source = "currency.code")
    @Mapping(target = "rateType",             expression = "java(entity.getRateType().name())")
    @Mapping(target = "status",               expression = "java(entity.getStatus().name())")
    RateResponseDto toDto(RateEntity entity);

    List<RateResponseDto> toDtoList(List<RateEntity> entities);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "client",           ignore = true)
    @Mapping(target = "developerProfile", ignore = true)
    @Mapping(target = "currency",         ignore = true)
    @Mapping(target = "rateType",         expression = "java(com.billingplatformapplication.rates.entity.RateEntity.RateType.valueOf(dto.rateType().toUpperCase()))")
    @Mapping(target = "status",           constant = "ACTIVE")
    @Mapping(target = "active",           ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    RateEntity toEntity(CreateRateRequestDto dto);
}