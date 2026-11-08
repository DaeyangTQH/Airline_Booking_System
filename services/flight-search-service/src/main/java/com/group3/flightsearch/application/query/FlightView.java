package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.model.FlightStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FlightView(UUID id, String flightCode, String originCode, String originCity,
        String destinationCode, String destinationCity, Instant departureTime, Instant arrivalTime,
        UUID aircraftId, String aircraftModel, FlightStatus status, List<FareView> fares) {}
