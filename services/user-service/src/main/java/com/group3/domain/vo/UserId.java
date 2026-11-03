package com.group3.domain.vo;

import com.group3.domain.exception.DomainValidationException;
import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        if (value == null) throw new DomainValidationException("User ID must not be null");
    }
    public static UserId newId() { return new UserId(UUID.randomUUID()); }
}
