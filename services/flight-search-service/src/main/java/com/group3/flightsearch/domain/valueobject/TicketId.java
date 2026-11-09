package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.UUID;

// External Booking reference; no cross-service database relationship.
public record TicketId(UUID value) {
    public TicketId {
        if (value == null) throw new InvalidDomainValueException("TicketId must not be null");
    }
}
