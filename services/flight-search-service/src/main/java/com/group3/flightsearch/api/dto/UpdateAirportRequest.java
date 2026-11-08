package com.group3.flightsearch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAirportRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String city,
        @NotBlank @Size(max = 255) String country
) {}
