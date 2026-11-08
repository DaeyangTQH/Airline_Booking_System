package com.group3.flightsearch.application.port.out;

import com.group3.flightsearch.application.query.FlightAvailabilityView;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FlightAvailabilityPort {
    List<FlightAvailabilityView> findByFlight(UUID flightId);
    List<FlightAvailabilityView> findByFlights(Collection<UUID> flightIds);
    // Must join the same transaction that writes Seat / creates Flight.
    void refresh(UUID flightId);
}
