package com.group3.flightsearch.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record FlightScheduleRequest(@NotNull Instant departureTime, @NotNull Instant arrivalTime) {}
