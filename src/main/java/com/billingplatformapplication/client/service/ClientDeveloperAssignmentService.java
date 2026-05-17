package com.billingplatformapplication.client.service;

import com.billingplatformapplication.client.dto.request.AssignDeveloperRequestDto;
import com.billingplatformapplication.client.dto.response.ClientDeveloperAssignmentResponseDto;
import com.billingplatformapplication.client.entity.ClientDeveloperAssignmentEntity;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.client.repository.ClientDeveloperAssignmentRepository;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.developers.entity.DeveloperEntity;
import com.billingplatformapplication.developers.repository.DeveloperRepository;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientDeveloperAssignmentService {

    private final ClientDeveloperAssignmentRepository assignmentRepository;
    private final ClientRepository clientRepository;
    private final DeveloperRepository developerRepository;

    @Transactional(readOnly = true)
    public List<ClientDeveloperAssignmentResponseDto> findByClient(UUID clientId) {
        return assignmentRepository.findActiveByClientWithDeveloper(clientId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public ClientDeveloperAssignmentResponseDto assign(UUID clientId,
                                                       AssignDeveloperRequestDto request) {
        if (assignmentRepository.existsByClientIdAndDeveloperIdAndActiveTrue(
                clientId, request.developerId())) {
            throw new DuplicateResourceException("Assignment", "developer",
                    request.developerId().toString());
        }

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
        DeveloperEntity developer = developerRepository.findById(request.developerId())
                .orElseThrow(() -> new ResourceNotFoundException("Developer", request.developerId()));

        ClientDeveloperAssignmentEntity entity = ClientDeveloperAssignmentEntity.builder()
                .client(client)
                .developer(developer)
                .notes(request.notes())
                .active(true)
                .build();

        return toDto(assignmentRepository.save(entity));
    }

    @Transactional
    public void unassign(UUID clientId, UUID developerId) {
        ClientDeveloperAssignmentEntity entity =
                assignmentRepository.findByClientAndDeveloper(clientId, developerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Assignment", developerId));
        entity.setActive(false);
        assignmentRepository.save(entity);
    }

    private ClientDeveloperAssignmentResponseDto toDto(ClientDeveloperAssignmentEntity e) {
        return new ClientDeveloperAssignmentResponseDto(
                e.getId(),
                e.getClient().getId(),
                e.getClient().getCompanyName(),
                e.getDeveloper().getId(),
                e.getDeveloper().getFullName(),
                e.getDeveloper().getDocumentId(),
                e.getDeveloper().getProfile() != null ? e.getDeveloper().getProfile().getName() : null,
                e.isActive(),
                e.getNotes(),
                e.getCreatedAt()
        );
    }
}