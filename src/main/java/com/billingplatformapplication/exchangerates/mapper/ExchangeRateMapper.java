package com.billingplatformapplication.exchangerates.mapper;

import com.billingplatformapplication.exchangerates.dto.request.CreateExchangeRateRequestDto;
import com.billingplatformapplication.exchangerates.dto.response.ExchangeRateResponseDto;
import com.billingplatformapplication.exchangerates.entity.ExchangeRateEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExchangeRateMapper {

    @Mapping(target = "fromCurrencyId",   source = "fromCurrency.id")
    @Mapping(target = "fromCurrencyCode", source = "fromCurrency.code")
    @Mapping(target = "toCurrencyId",     source = "toCurrency.id")
    @Mapping(target = "toCurrencyCode",   source = "toCurrency.code")
    ExchangeRateResponseDto toDto(ExchangeRateEntity entity);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "fromCurrency", ignore = true)
    @Mapping(target = "toCurrency",   ignore = true)
    @Mapping(target = "active",       constant = "true")
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    ExchangeRateEntity toEntity(CreateExchangeRateRequestDto dto);
}
