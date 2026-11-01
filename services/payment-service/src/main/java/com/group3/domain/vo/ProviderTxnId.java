package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record ProviderTxnId(String value) {
    public ProviderTxnId {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException(
                    "Provider transaction ID must not be blank"
            );
        }
        value = value.trim();
    }
}
