package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record PaymentId(UUID value) {
    public PaymentId {
        if (value == null) {
            throw new InvalidDomainValueException(
                    "Payment ID must not be null"
            );
        }
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }
}
