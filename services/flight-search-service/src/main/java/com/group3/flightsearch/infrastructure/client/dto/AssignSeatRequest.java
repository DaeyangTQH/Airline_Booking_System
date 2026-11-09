package com.group3.flightsearch.infrastructure.client.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignSeatRequest(@NotNull UUID seatId) {}
