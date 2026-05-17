package com.billingplatformapplication.users.usecase;


import com.billingplatformapplication.users.dto.request.CreateUserRequestDto;
import com.billingplatformapplication.users.dto.response.UserResponseDto;
import com.billingplatformapplication.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case that orchestrates user creation.
 * Delegates to UserService — exists as a named entry point
 * to keep the controller thin and allow future pre/post hooks.
 */
@Component
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserService userService;

    public UserResponseDto execute(CreateUserRequestDto request) {
        return userService.create(request);
    }
}
