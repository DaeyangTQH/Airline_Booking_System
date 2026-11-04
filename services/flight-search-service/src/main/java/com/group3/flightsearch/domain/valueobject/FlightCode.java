package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;

public record FlightCode(String value) {
    public FlightCode {
        if (value == null) {
            throw new InvalidDomainValueException("FlightCode must not be null");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new InvalidDomainValueException("FlightCode must not be blank");
        }
    }
}
