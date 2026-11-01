package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;

public record AircraftModel(String value) {
    public AircraftModel {
        if (value == null) {
            throw new InvalidDomainValueException("AircraftModel must not be null");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new InvalidDomainValueException("AircraftModel must not be blank");
        }
    }
}
