package com.billingplatformapplication.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {
    private boolean success;
    private String  message;
    private T       data;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponseDto<T> ok(T data) {
        return ApiResponseDto.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponseDto<T> ok(String message, T data) {
        return ApiResponseDto.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return ApiResponseDto.<T>builder().success(false).message(message).build();
    }
}
