package com.group3.domain.vo;

import com.group3.domain.exception.DomainValidationException;
import java.util.UUID;

public record PassengerId(UUID value) {
    public PassengerId {
        if (value == null) throw new DomainValidationException("Passenger ID must not be null");
    }
    public static PassengerId newId() { return new PassengerId(UUID.randomUUID()); }
}
