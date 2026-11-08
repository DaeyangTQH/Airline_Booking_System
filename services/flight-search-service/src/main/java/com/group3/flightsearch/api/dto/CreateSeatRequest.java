package com.group3.flightsearch.api.dto;

import com.group3.flightsearch.domain.model.FareClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSeatRequest(@NotBlank @Size(max = 255) String seatNumber, @NotNull FareClass seatClass) {}
