package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record ProviderRefundId(String value) {
    public ProviderRefundId {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException(
                    "Provider refund ID must not be blank"
            );
        }
        value = value.trim();
    }
}
