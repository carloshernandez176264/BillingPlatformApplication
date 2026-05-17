package com.billingplatformapplication.developerprofiles.mapper;

import com.billingplatformapplication.developerprofiles.dto.request.CreateDeveloperProfileRequestDto;
import com.billingplatformapplication.developerprofiles.dto.response.DeveloperProfileResponseDto;
import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeveloperProfileMapper {

    DeveloperProfileResponseDto toDto(DeveloperProfileEntity entity);

    List<DeveloperProfileResponseDto> toDtoList(List<DeveloperProfileEntity> entities);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "active",    constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    DeveloperProfileEntity toEntity(CreateDeveloperProfileRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(CreateDeveloperProfileRequestDto dto,
                      @MappingTarget DeveloperProfileEntity entity);
}

