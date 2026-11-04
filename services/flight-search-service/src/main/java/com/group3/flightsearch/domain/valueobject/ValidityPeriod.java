package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.time.Instant;

public record ValidityPeriod(Instant from, Instant to) {
    public ValidityPeriod {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new InvalidDomainValueException("Validity start must be before end");
        }
    }

    public boolean contains(Instant instant) {
        if (instant == null) {
            throw new InvalidDomainValueException("Instant must not be null");
        }
        return !instant.isBefore(from) && instant.isBefore(to);
    }

    public boolean overlaps(ValidityPeriod other) {
        if (other == null) {
            throw new InvalidDomainValueException("Period must not be null");
        }
        return from.isBefore(other.to) && other.from.isBefore(to);
    }
}
