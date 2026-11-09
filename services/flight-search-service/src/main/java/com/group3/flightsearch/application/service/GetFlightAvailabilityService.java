package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.in.GetFlightAvailabilityUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.FlightAvailabilityView;
import com.group3.flightsearch.domain.valueobject.FlightId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFlightAvailabilityService implements GetFlightAvailabilityUseCase {
    private final FlightQueryPort flights;

    @Override
    public List<FlightAvailabilityView> getByFlight(UUID flightId) {
        new FlightId(flightId);
        var flight = flights.findById(flightId);
        if (flight.isEmpty()) {
            throw new CustomException("FLIGHT_NOT_FOUND", ErrorType.NOT_FOUND, "Flight not found: " + flightId);
        }
        return flight.get().availability();
    }

}
