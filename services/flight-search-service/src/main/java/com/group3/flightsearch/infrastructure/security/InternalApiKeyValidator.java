package com.group3.flightsearch.infrastructure.security;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiKeyValidator {
    private final byte[] expectedApiKey;

    public InternalApiKeyValidator(@Value("${app.internal-api-key}") String expectedApiKey) {
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            throw new IllegalStateException("INTERNAL_API_KEY must not be blank");
        }
        this.expectedApiKey = expectedApiKey.getBytes(StandardCharsets.UTF_8);
    }

    public void validate(String providedApiKey) {
        if (providedApiKey == null || providedApiKey.isBlank()
                || !MessageDigest.isEqual(expectedApiKey, providedApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new CustomException("INTERNAL_AUTHENTICATION_FAILED", ErrorType.UNAUTHORIZED,
                    "Internal API key is missing or invalid");
        }
    }
}
