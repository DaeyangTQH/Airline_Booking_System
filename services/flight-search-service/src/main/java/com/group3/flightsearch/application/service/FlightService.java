package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.in.FlightUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService implements FlightUseCase {
    private final FlightQueryPort queryPort;

    @Override
    public FlightView getById(UUID id) {
        new FlightId(id);
        var flight = queryPort.findById(id);
        if (flight.isEmpty()) {
            throw missing("FLIGHT_NOT_FOUND", "Flight not found: " + id);
        }
        return flight.get();
    }

    @Override
    public List<FlightView> search(String origin, String destination, LocalDate date,
                                    FareClass fareClass, int quantity, int page, int size) {
        if (quantity < 1 || quantity > 9) {
            throw new InvalidDomainValueException("Quantity must be between 1 and 9");
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        String originCode = origin == null ? null : new AirportCode(origin).value();
        String destinationCode = destination == null ? null : new AirportCode(destination).value();
        if (originCode != null && originCode.equals(destinationCode)) {
            throw new InvalidDomainValueException("Origin and destination must differ");
        }
        return queryPort.search(new FlightSearchQuery(originCode, destinationCode, date,
                fareClass, Instant.now(), quantity, page, size));
    }

    private CustomException missing(String code, String message) {
        return new CustomException(code, ErrorType.NOT_FOUND, message);
    }
}
