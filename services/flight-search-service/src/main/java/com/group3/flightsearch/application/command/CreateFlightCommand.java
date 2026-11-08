package com.group3.flightsearch.application.command;

import java.time.Instant;
import java.util.UUID;

public record CreateFlightCommand(String flightCode, String originCode, String destinationCode,
        Instant departureTime, Instant arrivalTime, UUID aircraftId) {}
