package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

public record SeatId(UUID value) {
    public SeatId {
        if (value == null) {
            throw new InvalidDomainValueException("SeatId must not be null");
        }
    }
}
