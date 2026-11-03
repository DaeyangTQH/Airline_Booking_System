package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record RefundId(UUID value) {
    public RefundId {
        if (value == null) {
            throw new InvalidDomainValueException(
                    "Refund ID must not be null"
            );
        }
    }

    public static RefundId generate() {
        return new RefundId(UUID.randomUUID());
    }
}
