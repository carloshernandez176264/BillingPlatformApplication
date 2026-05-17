package com.billingplatformapplication.users.mapper;


import com.billingplatformapplication.users.dto.response.UserResponseDto;
import com.billingplatformapplication.users.entity.UserEntity;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "roles",  expression = "java(mapRoleNames(entity))")
    UserResponseDto toDto(UserEntity entity);

    default Set<String> mapRoleNames(UserEntity entity) {
        if (entity.getRoles() == null) return Set.of();
        return entity.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());
    }
}
