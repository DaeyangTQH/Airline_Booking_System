package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.Locale;

public record SeatNumber(String value) {
    public SeatNumber {
        if (value == null) {
            throw new InvalidDomainValueException("SeatNumber must not be null");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!value.matches("[1-9][0-9]*[A-Z]")) {
            throw new InvalidDomainValueException("SeatNumber must look like 12A");
        }
    }
}
