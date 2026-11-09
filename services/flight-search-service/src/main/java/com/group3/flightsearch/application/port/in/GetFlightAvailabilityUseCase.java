package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.query.FlightAvailabilityView;
import java.util.List;
import java.util.UUID;

public interface GetFlightAvailabilityUseCase {
    List<FlightAvailabilityView> getByFlight(UUID flightId);
}
