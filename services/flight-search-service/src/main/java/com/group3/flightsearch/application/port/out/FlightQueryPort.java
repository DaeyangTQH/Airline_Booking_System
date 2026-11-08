package com.group3.flightsearch.application.port.out;

import com.group3.flightsearch.application.query.FlightSearchQuery;
import com.group3.flightsearch.application.query.FlightView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlightQueryPort {
    Optional<FlightView> findById(UUID id);
    List<FlightView> search(FlightSearchQuery query);
}
