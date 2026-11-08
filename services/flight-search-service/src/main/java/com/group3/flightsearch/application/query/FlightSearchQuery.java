package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.model.FareClass;
import java.time.Instant;
import java.time.LocalDate;

public record FlightSearchQuery(String originCode, String destinationCode, LocalDate departureDate,
        FareClass fareClass, Instant pricedAt, int quantity, int page, int size) {}
