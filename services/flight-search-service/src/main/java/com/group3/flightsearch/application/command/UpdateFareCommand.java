package com.group3.flightsearch.application.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateFareCommand(UUID flightId, UUID fareId, BigDecimal basePrice,
        String currency, Instant validFrom, Instant validTo) {}
