package com.billingplatformapplication.developerprofiles.service;


import com.billingplatformapplication.developerprofiles.dto.request.CreateDeveloperProfileRequestDto;
import com.billingplatformapplication.developerprofiles.dto.response.DeveloperProfileResponseDto;
import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.developerprofiles.mapper.DeveloperProfileMapper;
import com.billingplatformapplication.developerprofiles.repository.DeveloperProfileRepository;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeveloperProfileService {

    private final DeveloperProfileRepository profileRepository;
    private final DeveloperProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public List<DeveloperProfileResponseDto> findAllActive() {
        return profileMapper.toDtoList(profileRepository.findByActiveTrue());
    }

    @Transactional(readOnly = true)
    public PageResponseDto<DeveloperProfileResponseDto> search(String search, Pageable pageable) {
        return PageResponseDto.from(
                profileRepository.searchActive(search, pageable).map(profileMapper::toDto));
    }

    @Transactional(readOnly = true)
    public DeveloperProfileResponseDto findById(UUID id) {
        return profileMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public DeveloperProfileEntity findEntityById(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeveloperProfile", id));
    }

    @Transactional
    public DeveloperProfileResponseDto create(CreateDeveloperProfileRequestDto request) {
        if (profileRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("DeveloperProfile", "name", request.name());
        }
        return profileMapper.toDto(
                profileRepository.save(profileMapper.toEntity(request)));
    }

    @Transactional
    public DeveloperProfileResponseDto update(UUID id, CreateDeveloperProfileRequestDto request) {
        DeveloperProfileEntity entity = findEntityById(id);
        profileMapper.updateEntity(request, entity);
        return profileMapper.toDto(profileRepository.save(entity));
    }

    @Transactional
    public void deactivate(UUID id) {
        DeveloperProfileEntity entity = findEntityById(id);
        entity.setActive(false);
        profileRepository.save(entity);
    }
}

