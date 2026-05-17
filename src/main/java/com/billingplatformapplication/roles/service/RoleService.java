package com.billingplatformapplication.roles.service;


import com.billingplatformapplication.roles.entity.RoleEntity;
import com.billingplatformapplication.roles.repository.RoleRepository;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleEntity> findAll() {
        return roleRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public RoleEntity findById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    @Transactional(readOnly = true)
    public RoleEntity findByName(String name) {
        return roleRepository.findByNameWithPermissions(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
    }
}

