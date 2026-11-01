package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

public record FareId(UUID value) {
    public FareId {
        if (value == null) {
            throw new InvalidDomainValueException("FareId must not be null");
        }
    }
}
