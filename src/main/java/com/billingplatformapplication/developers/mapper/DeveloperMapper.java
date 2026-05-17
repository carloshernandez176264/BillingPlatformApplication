package com.billingplatformapplication.developers.mapper;

import com.billingplatformapplication.developers.dto.request.CreateDeveloperRequestDto;
import com.billingplatformapplication.developers.dto.response.DeveloperResponseDto;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeveloperMapper {

    @Mapping(target = "profileId",   source = "profile.id")
    @Mapping(target = "profileName", source = "profile.name")
    @Mapping(target = "status",      expression = "java(entity.getStatus().name())")
    @Mapping(target = "baseSalary",  source = "baseSalary")
    DeveloperResponseDto toDto(DeveloperEntity entity);

    List<DeveloperResponseDto> toDtoList(List<DeveloperEntity> entities);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "profile",    ignore = true)
    @Mapping(target = "status",     constant = "ACTIVE")
    @Mapping(target = "active",     constant = "true")
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    DeveloperEntity toEntity(CreateDeveloperRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "profile",    ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    void updateEntity(CreateDeveloperRequestDto dto, @MappingTarget DeveloperEntity entity);
}