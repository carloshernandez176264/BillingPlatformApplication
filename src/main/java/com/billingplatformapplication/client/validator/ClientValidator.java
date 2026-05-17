package com.billingplatformapplication.client.validator;

import com.billingplatformapplication.client.dto.request.CreateClientRequestDto;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.shared.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientValidator {

    private final ClientRepository clientRepository;

    public void validateCreate(CreateClientRequestDto request) {
        if (clientRepository.existsByTaxId(request.taxId())) {
            throw new DuplicateResourceException("Client", "taxId", request.taxId());
        }
    }
}