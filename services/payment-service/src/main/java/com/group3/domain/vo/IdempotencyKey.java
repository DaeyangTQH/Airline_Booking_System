package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record IdempotencyKey(String value) {
    public IdempotencyKey {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException(
                    "Idempotency key must not be blank"
            );
        }
        value = value.trim();
    }
}
