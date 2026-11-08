package com.group3.flightsearch.application.command;

import java.time.Instant;
import java.util.UUID;

public record UpdateFlightScheduleCommand(UUID flightId, Instant departureTime, Instant arrivalTime) {}
