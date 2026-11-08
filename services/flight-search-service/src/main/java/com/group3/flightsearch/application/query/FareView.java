package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.model.Fare;
import com.group3.flightsearch.domain.model.FareClass;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FareView(UUID id, UUID flightId, FareClass fareClass, BigDecimal basePrice,
        String currency, Instant validFrom, Instant validTo) {
    public static FareView from(Fare fare) {
        return new FareView(fare.getId().value(), fare.getFlightId().value(), fare.getFareClass(),
                fare.getBasePrice().amount(), fare.getBasePrice().currency().getCurrencyCode(),
                fare.getValidity().from(), fare.getValidity().to());
    }
}
