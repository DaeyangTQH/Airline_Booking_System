package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

public record AirportId(UUID value) {
    public AirportId {
        if (value == null) {
            throw new InvalidDomainValueException("AirportId must not be null");
        }
    }
}
