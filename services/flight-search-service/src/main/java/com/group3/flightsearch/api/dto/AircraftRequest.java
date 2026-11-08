package com.group3.flightsearch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AircraftRequest(
        @NotBlank @Size(max = 255) String model,
        @NotNull @Positive Integer totalSeats
) {}
