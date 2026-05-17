package com.billingplatformapplication.worklogs.mapper;

import com.billingplatformapplication.worklogs.dto.request.CreateWorkLogRequestDto;
import com.billingplatformapplication.worklogs.dto.response.WorkLogResponseDto;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkLogMapper {

    @Mapping(target = "clientId",             source = "client.id")
    @Mapping(target = "clientName",           source = "client.companyName")
    @Mapping(target = "developerId",          source = "developer.id")
    @Mapping(target = "developerName",        source = "developer.fullName")
    @Mapping(target = "developerProfileId",   source = "developerProfile.id")
    @Mapping(target = "developerProfileName", source = "developerProfile.name")
    @Mapping(target = "appliedRateId",        source = "appliedRate.id")
    @Mapping(target = "appliedRateType",      expression = "java(entity.getAppliedRate() != null ? entity.getAppliedRate().getRateType().name() : null)")
    @Mapping(target = "status",               expression = "java(entity.getStatus().name())")
    WorkLogResponseDto toDto(WorkLogEntity entity);

    List<WorkLogResponseDto> toDtoList(List<WorkLogEntity> entities);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "client",           ignore = true)
    @Mapping(target = "developer",        ignore = true)
    @Mapping(target = "developerProfile", ignore = true)
    @Mapping(target = "appliedRate",      ignore = true)
    @Mapping(target = "billableAmount",   ignore = true)
    @Mapping(target = "status",           constant = "DRAFT")
    @Mapping(target = "active",           constant = "true")
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    WorkLogEntity toEntity(CreateWorkLogRequestDto dto);
}
