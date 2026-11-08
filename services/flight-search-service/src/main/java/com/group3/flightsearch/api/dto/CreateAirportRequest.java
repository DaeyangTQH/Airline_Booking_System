package com.group3.flightsearch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAirportRequest(
        @NotBlank @Size(max = 3) String code,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String city,
        @NotBlank @Size(max = 255) String country
) {}
