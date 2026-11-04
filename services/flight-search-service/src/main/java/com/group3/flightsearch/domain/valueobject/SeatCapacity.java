package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;

public record SeatCapacity(int value) {
    public SeatCapacity {
        if (value <= 0) {
            throw new InvalidDomainValueException("Seat capacity must be positive");
        }
    }
}
