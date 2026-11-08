package com.group3.flightsearch.api.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record CreateFlightRequest(
        @NotBlank @Size(max = 255) String flightCode,
        @NotBlank @Size(max = 3) String originCode,
        @NotBlank @Size(max = 3) String destinationCode,
        @NotNull Instant departureTime, @NotNull Instant arrivalTime, @NotNull UUID aircraftId) {}
