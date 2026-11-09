package com.group3.flightsearch.infrastructure.client.dto;

import com.group3.flightsearch.application.query.FareQuoteView;
import java.math.BigDecimal;
import java.util.UUID;

/** Wire contract consumed by Booking Service's FlightFareResponse. */
public record FareQuoteResponse(UUID flightId, String fareClass, BigDecimal unitPrice,
        String currency, int availableSeats, boolean bookable) {
    public static FareQuoteResponse from(FareQuoteView quote) {
        return new FareQuoteResponse(quote.flightId(), quote.fareClass().name(), quote.unitPrice(),
                quote.currency(), quote.availableSeats(), quote.bookable());
    }
}
