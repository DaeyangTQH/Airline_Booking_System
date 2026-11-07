package com.group3.infrastructure.security;

import com.group3.application.exception.InternalAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiKeyValidator {

    private final byte[] expectedApiKey;

    public InternalApiKeyValidator(
            @Value("${app.internal-api-key}") String expectedApiKey
    ) {

        this.expectedApiKey =
                expectedApiKey.getBytes(
                        StandardCharsets.UTF_8
                );
    }

    public void validate(String providedApiKey) {

        if(providedApiKey == null || providedApiKey.isBlank()){
            throw new InternalAuthenticationException(
                    "Internal API key is missing in the request"
            );
        }

        boolean valid = MessageDigest.isEqual(
                expectedApiKey,
                providedApiKey.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        if(!valid) {
            throw new InternalAuthenticationException(
                    "Internal API key is invalid"
            );
        }
    }
}
