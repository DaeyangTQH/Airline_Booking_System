package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

public record AircraftId(UUID value) {
    public AircraftId {
        if (value == null) {
            throw new InvalidDomainValueException("AircraftId must not be null");
        }
    }
}
