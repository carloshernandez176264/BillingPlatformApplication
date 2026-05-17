package com.billingplatformapplication.client.mapper;



import com.billingplatformapplication.client.dto.request.CreateClientRequestDto;
import com.billingplatformapplication.client.dto.request.UpdateClientRequestDto;
import com.billingplatformapplication.client.dto.response.ClientResponseDto;
import com.billingplatformapplication.client.entity.ClientEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "primaryCurrencyId",   source = "primaryCurrency.id")
    @Mapping(target = "primaryCurrencyCode", source = "primaryCurrency.code")
    @Mapping(target = "status",              expression = "java(entity.getStatus().name())")
    ClientResponseDto toDto(ClientEntity entity);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "primaryCurrency", ignore = true)
    @Mapping(target = "status",          constant = "ACTIVE")
    @Mapping(target = "active",          constant = "true")
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    ClientEntity toEntity(CreateClientRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "taxId",           ignore = true)
    @Mapping(target = "primaryCurrency", ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    void updateEntity(UpdateClientRequestDto dto, @MappingTarget ClientEntity entity);
}

