package com.group3.flightsearch.domain.repository;

import com.group3.flightsearch.domain.aggregate.Flight;
import com.group3.flightsearch.domain.valueobject.FlightId;
import java.util.Optional;

public interface FlightRepository {
    Flight save(Flight flight);
    Optional<Flight> findByIdForUpdate(FlightId id);
}
