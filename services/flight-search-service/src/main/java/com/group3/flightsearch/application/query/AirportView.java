package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.aggregate.Airport;
import java.util.UUID;

public record AirportView(UUID id, String code, String name, String city, String country) {
    public static AirportView from(Airport airport) {
        return new AirportView(airport.getId().value(), airport.getCode().value(),
                airport.getName(), airport.getCity(), airport.getCountry());
    }
}
