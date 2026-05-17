package com.billingplatformapplication.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/** Returned specifically for token-refresh operations. */
@Getter
@Builder
public class RefreshTokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long   expiresIn;
}
