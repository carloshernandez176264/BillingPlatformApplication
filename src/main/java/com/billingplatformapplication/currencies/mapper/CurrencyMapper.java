package com.billingplatformapplication.currencies.mapper;


import com.billingplatformapplication.currencies.dto.request.CreateCurrencyRequestDto;
import com.billingplatformapplication.currencies.dto.response.CurrencyResponseDto;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper {

    CurrencyResponseDto toDto(CurrencyEntity entity);
    List<CurrencyResponseDto> toDtoList(List<CurrencyEntity> entities);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "active",    constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CurrencyEntity toEntity(CreateCurrencyRequestDto dto);
}