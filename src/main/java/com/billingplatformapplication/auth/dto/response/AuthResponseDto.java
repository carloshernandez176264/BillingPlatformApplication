package com.billingplatformapplication.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private boolean mustChangePassword;
}
