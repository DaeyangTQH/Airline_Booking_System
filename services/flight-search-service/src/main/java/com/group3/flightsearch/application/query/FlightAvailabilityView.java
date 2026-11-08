package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.model.FareClass;
import java.util.UUID;

public record FlightAvailabilityView(UUID flightId, FareClass fareClass, int availableSeats, int classCapacity) {}
