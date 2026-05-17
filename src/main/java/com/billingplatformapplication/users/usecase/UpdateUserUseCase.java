package com.billingplatformapplication.users.usecase;


import com.billingplatformapplication.users.dto.request.UpdateUserRequestDto;
import com.billingplatformapplication.users.dto.response.UserResponseDto;
import com.billingplatformapplication.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserService userService;

    public UserResponseDto execute(UUID id, UpdateUserRequestDto request) {
        return userService.update(id, request);
    }
}

