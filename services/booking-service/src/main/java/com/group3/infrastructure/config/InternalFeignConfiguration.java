package com.group3.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalFeignConfiguration {

    private static final String INTERNAL_API_KEY_HEADER =
            "X-Internal-Api-Key";

    @Bean
    public RequestInterceptor internalApiKeyRequestInterceptor(
            @Value("${app.internal-api-key}") String internalApiKey
    ) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalStateException(
                    "Internal API key must not be blank"
            );
        }

        return requestTemplate -> requestTemplate.header(
                INTERNAL_API_KEY_HEADER,
                internalApiKey
        );
    }
}
