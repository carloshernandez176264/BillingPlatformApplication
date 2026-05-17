package com.billingplatformapplication.developers.service;

import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import com.billingplatformapplication.developerprofiles.service.DeveloperProfileService;
import com.billingplatformapplication.developers.dto.request.CreateDeveloperRequestDto;
import com.billingplatformapplication.developers.dto.response.DeveloperResponseDto;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.developers.mapper.DeveloperMapper;
import com.billingplatformapplication.developers.repository.DeveloperRepository;
import com.billingplatformapplication.shared.dto.PageResponseDto;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperProfileService profileService;
    private final DeveloperMapper developerMapper;

    @Transactional(readOnly = true)
    public PageResponseDto<DeveloperResponseDto> search(String search, String status,
                                                        UUID profileId, Pageable pageable) {
        // Si search es null o vacío → null para que el IS NULL del WHERE aplique
        String searchParam = (search == null || search.isBlank()) ? null : search.trim();

        // El patrón ya viene en minúsculas — así evitamos LOWER() sobre el parámetro
        String searchPattern = searchParam != null ? "%" + searchParam.toLowerCase() + "%" : null;

        DeveloperEntity.DeveloperStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = DeveloperEntity.DeveloperStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return PageResponseDto.from(
                developerRepository.searchWithFilters(
                        searchParam,
                        searchPattern,
                        statusEnum,
                        profileId,
                        pageable
                ).map(developerMapper::toDto));
    }

    @Transactional(readOnly = true)
    public DeveloperResponseDto findById(UUID id) {
        return developerMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public DeveloperEntity findEntityById(UUID id) {
        return developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", id));
    }

    @Transactional
    public DeveloperResponseDto create(CreateDeveloperRequestDto request) {
        if (developerRepository.existsByDocumentId(request.documentId())) {
            throw new DuplicateResourceException("Developer", "documentId", request.documentId());
        }
        DeveloperProfileEntity profile = profileService.findEntityById(request.profileId());
        DeveloperEntity entity = developerMapper.toEntity(request);
        entity.setProfile(profile);
        return developerMapper.toDto(developerRepository.save(entity));
    }

    @Transactional
    public DeveloperResponseDto update(UUID id, CreateDeveloperRequestDto request) {
        DeveloperEntity entity = findEntityById(id);
        developerMapper.updateEntity(request, entity);
        entity.setProfile(profileService.findEntityById(request.profileId()));
        return developerMapper.toDto(developerRepository.save(entity));
    }

    @Transactional
    public void deactivate(UUID id) {
        DeveloperEntity entity = findEntityById(id);
        entity.setActive(false);
        entity.setStatus(DeveloperEntity.DeveloperStatus.INACTIVE);
        developerRepository.save(entity);
    }
}