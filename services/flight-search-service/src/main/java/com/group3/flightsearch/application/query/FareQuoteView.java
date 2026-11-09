package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.model.FareClass;
import java.math.BigDecimal;
import java.util.UUID;

// Application result; internal HTTP responses are mapped in infrastructure.
public record FareQuoteView(UUID flightId, FareClass fareClass, BigDecimal unitPrice,
        String currency, int availableSeats, boolean bookable) {}
