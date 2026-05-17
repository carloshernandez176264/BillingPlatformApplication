package com.billingplatformapplication.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
    private int                 status;
    private String              error;
    private String              message;
    private String              path;
    private String              requestId;
    private Map<String, String> fieldErrors;
    @Builder.Default
    private Instant             timestamp = Instant.now();
}
