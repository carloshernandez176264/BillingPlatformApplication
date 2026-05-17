package com.billingplatformapplication.users.service;


import com.billingplatformapplication.audit.service.AuditService;
import com.billingplatformapplication.roles.entity.RoleEntity;
import com.billingplatformapplication.roles.repository.RoleRepository;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import com.billingplatformapplication.users.dto.request.CreateUserRequestDto;
import com.billingplatformapplication.users.dto.request.UpdateUserRequestDto;
import com.billingplatformapplication.users.dto.response.UserResponseDto;
import com.billingplatformapplication.users.entity.UserEntity;
import com.billingplatformapplication.users.mapper.UserMapper;
import com.billingplatformapplication.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> findAll(String search, String status,
                                                    Pageable pageable) {
        UserEntity.UserStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = UserEntity.UserStatus.valueOf(status.toUpperCase());
        }

        String searchParam   = (search == null || search.isBlank()) ? null : search.trim();
        String searchPattern = searchParam != null ? "%" + searchParam.toLowerCase() + "%" : null;

        return PageResponseDto.from(
                userRepository.findAllWithFilters(searchParam, searchPattern, statusEnum, pageable)
                        .map(userMapper::toDto));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(UUID id) {
        return userMapper.toDto(findEntityById(id));
    }

    @Transactional
    public UserResponseDto create(CreateUserRequestDto request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        Set<RoleEntity> roles = resolveRoles(request.roleIds());

        UserEntity entity = UserEntity.builder()
                .email(request.email().toLowerCase())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserEntity.UserStatus.ACTIVE)
                .mustChangePassword(true)
                .passwordChangedAt(Instant.now())
                .roles(roles)
                .build();

        UserEntity saved = userRepository.save(entity);
        auditService.log("USER", saved.getId().toString(), "CREATE",
                "system", null,
                Map.of("email", saved.getEmail(),
                        "roles", roles.stream().map(RoleEntity::getName)
                                .collect(Collectors.joining(","))));
        log.info("User created: {}", saved.getEmail());
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserResponseDto update(UUID id, UpdateUserRequestDto request) {
        UserEntity entity = findEntityById(id);

        if (request.fullName() != null) entity.setFullName(request.fullName());
        if (request.status()   != null) {
            entity.setStatus(UserEntity.UserStatus.valueOf(request.status().toUpperCase()));
            entity.setActive(entity.getStatus() == UserEntity.UserStatus.ACTIVE);
        }
        if (request.locked() != null) {
            entity.setLocked(request.locked());
            if (Boolean.FALSE.equals(request.locked())) entity.setFailedLoginAttempts(0);
        }
        if (request.mustChangePassword() != null) {
            entity.setMustChangePassword(request.mustChangePassword());
        }
        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            entity.setRoles(resolveRoles(request.roleIds()));
        }
        auditService.log("USER", id.toString(), "UPDATE", "admin", null, null);
        return userMapper.toDto(userRepository.save(entity));
    }

    @Transactional
    public void unlock(UUID id) {
        UserEntity entity = findEntityById(id);
        entity.setLocked(false);
        entity.setFailedLoginAttempts(0);
        userRepository.save(entity);
        auditService.log("USER", id.toString(), "STATUS_CHANGE", "admin",
                null, Map.of("action", "UNLOCK"));
    }

    @Transactional
    public void deactivate(UUID id) {
        UserEntity entity = findEntityById(id);
        entity.setActive(false);
        entity.setStatus(UserEntity.UserStatus.INACTIVE);
        userRepository.save(entity);
        auditService.log("USER", id.toString(), "DELETE", "admin", null, null);
    }

    public UserEntity findEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Set<RoleEntity> resolveRoles(Set<UUID> roleIds) {
        Set<RoleEntity> roles = new HashSet<>();
        for (UUID roleId : roleIds) {
            roles.add(roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId)));
        }
        return roles;
    }
}

