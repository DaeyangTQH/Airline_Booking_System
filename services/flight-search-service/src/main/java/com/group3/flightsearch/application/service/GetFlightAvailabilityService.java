package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.in.GetFlightAvailabilityUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.FlightAvailabilityView;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.FareClass;
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
        return flights.findById(flightId).orElseThrow(() -> new CustomException("FLIGHT_NOT_FOUND",
                ErrorType.NOT_FOUND, "Flight not found: " + flightId)).availability();
    }

    @Override
    public FlightAvailabilityView getByClass(UUID flightId, FareClass fareClass) {
        if (fareClass == null) throw new InvalidDomainValueException("Fare class is required");
        return getByFlight(flightId).stream().filter(row -> row.fareClass() == fareClass)
                .findFirst().orElseThrow(() -> new CustomException("AVAILABILITY_NOT_FOUND",
                        ErrorType.NOT_FOUND, "Availability projection not found for this flight and class"));
    }
}
