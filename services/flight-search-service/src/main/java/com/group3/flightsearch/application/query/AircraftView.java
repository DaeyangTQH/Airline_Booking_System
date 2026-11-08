package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.aggregate.Aircraft;
import java.util.UUID;

public record AircraftView(UUID id, String model, int totalSeats) {
    public static AircraftView from(Aircraft aircraft) {
        return new AircraftView(aircraft.getId().value(), aircraft.getModel().value(),
                aircraft.getTotalSeats().value());
    }
}
