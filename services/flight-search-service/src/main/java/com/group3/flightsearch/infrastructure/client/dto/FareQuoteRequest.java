package com.group3.flightsearch.infrastructure.client.dto;

import com.group3.flightsearch.domain.model.FareClass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Query parameters for Booking's internal fare quote contract. */
public record FareQuoteRequest(@NotNull FareClass fareClass, @NotNull @Positive Integer quantity) {}
