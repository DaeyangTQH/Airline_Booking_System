package com.group3.infrastructure.client.flight.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FlightFareResponse(
        UUID flightId,
        String fareClass,
        BigDecimal unitPrice,
        String currency,
        int availableSeats,
        boolean bookable
) {
}
