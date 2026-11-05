package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

public record FlightId(UUID value) {
    public FlightId {
        if (value == null) {
            throw new InvalidDomainValueException("FlightId must not be null");
        }
    }
}
