package com.billingplatformapplication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.cors")
public class CorsConfig {
    private List<String> allowedOrigins = List.of("http://localhost:4200");
}
