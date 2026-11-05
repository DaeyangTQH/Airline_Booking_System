package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

public record BookingId(UUID value) {
    public BookingId {
        if (value == null) {
            throw new InvalidDomainValueException("BookingId must not be null");
        }
    }
}
