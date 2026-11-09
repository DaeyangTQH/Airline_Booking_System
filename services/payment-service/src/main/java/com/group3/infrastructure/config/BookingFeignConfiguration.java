package com.group3.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BookingFeignConfiguration {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    @Bean
    public RequestInterceptor bookingInternalApiKeyInterceptor(
            @Value("${app.internal-api-key}") String internalApiKey
    ) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalStateException("Internal API key must not be blank");
        }
        return template -> template.header(INTERNAL_API_KEY_HEADER, internalApiKey);
    }
}
