package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.in.SeatUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.SeatView;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.repository.*;
import com.group3.flightsearch.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService implements SeatUseCase {
    private final SeatRepository seats;
    private final FlightQueryPort flightQuery;

    @Override
    public List<SeatView> list(UUID flightId, FareClass seatClass, int page, int size) {
        FlightId id = new FlightId(flightId);
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        if (flightQuery.findById(flightId).isEmpty()) {
            throw error("FLIGHT_NOT_FOUND", ErrorType.NOT_FOUND, "Flight not found: " + flightId);
        }
        List<SeatView> result = new ArrayList<>();
        for (var seat : seats.findByFlight(id, seatClass, page, size)) {
            result.add(SeatView.from(seat));
        }
        return List.copyOf(result);
    }

    private CustomException error(String code, ErrorType type, String message) {
        return new CustomException(code, type, message);
    }
}
