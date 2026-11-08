package com.group3.flightsearch.application.command;

import com.group3.flightsearch.domain.model.FareClass;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateFareCommand(UUID flightId, FareClass fareClass, BigDecimal basePrice,
        String currency, Instant validFrom, Instant validTo) {}
