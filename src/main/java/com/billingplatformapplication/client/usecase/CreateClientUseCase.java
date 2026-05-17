package com.billingplatformapplication.client.usecase;


import com.billingplatformapplication.client.dto.request.CreateClientRequestDto;
import com.billingplatformapplication.client.dto.response.ClientResponseDto;
import com.billingplatformapplication.client.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateClientUseCase {

    private final ClientService clientService;

    public ClientResponseDto execute(CreateClientRequestDto request) {
        return clientService.create(request);
    }
}
