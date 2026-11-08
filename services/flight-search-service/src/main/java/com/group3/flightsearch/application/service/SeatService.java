package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.command.CreateSeatCommand;
import com.group3.flightsearch.application.port.in.SeatUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.SeatView;
import com.group3.flightsearch.domain.aggregate.Seat;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.model.FlightStatus;
import com.group3.flightsearch.domain.repository.*;
import com.group3.flightsearch.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService implements SeatUseCase {
    private final SeatRepository seats;
    private final FlightRepository flights;
    private final AircraftRepository aircrafts;
    private final FlightQueryPort flightQuery;

    @Override
    @Transactional
    public SeatView create(CreateSeatCommand command) {
        if (command == null) throw new InvalidDomainValueException("Create seat command is required");
        FlightId flightId = new FlightId(command.flightId());
        SeatNumber number = new SeatNumber(command.seatNumber());
        if (command.seatClass() == null) throw new InvalidDomainValueException("Seat class is required");
        // Serialize all seat additions to a flight, then coordinate with aircraft capacity updates.
        var flight = flights.findByIdForUpdate(flightId)
                .orElseThrow(() -> error("FLIGHT_NOT_FOUND", ErrorType.NOT_FOUND, "Flight not found: " + command.flightId()));
        if ((flight.getStatus() != FlightStatus.SCHEDULED && flight.getStatus() != FlightStatus.DELAYED)
                || !flight.getDepartureTime().isAfter(Instant.now())) {
            throw error("FLIGHT_NOT_OPEN_FOR_SEATS", ErrorType.CONFLICT, "Cannot add seats to an inactive or departed flight");
        }
        var aircraft = aircrafts.findByIdForUpdate(flight.getAircraftId())
                .orElseThrow(() -> error("AIRCRAFT_NOT_FOUND", ErrorType.NOT_FOUND, "Aircraft does not exist"));
        if (seats.existsByFlightAndNumber(flightId, number)) {
            throw error("SEAT_NUMBER_ALREADY_EXISTS", ErrorType.CONFLICT, "Seat already exists on this flight: " + number.value());
        }
        if (seats.countByFlight(flightId) >= aircraft.getTotalSeats().value()) {
            throw error("FLIGHT_SEAT_CAPACITY_EXCEEDED", ErrorType.CONFLICT, "Assigned seats have reached aircraft capacity");
        }
        return SeatView.from(seats.save(Seat.create(new SeatId(UUID.randomUUID()), flightId, number, command.seatClass())));
    }

    @Override
    public SeatView getById(UUID id) {
        return seats.findById(new SeatId(id)).map(SeatView::from)
                .orElseThrow(() -> error("SEAT_NOT_FOUND", ErrorType.NOT_FOUND, "Seat not found: " + id));
    }

    @Override
    public List<SeatView> list(UUID flightId, FareClass seatClass, int page, int size) {
        FlightId id = new FlightId(flightId);
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        if (flightQuery.findById(flightId).isEmpty()) {
            throw error("FLIGHT_NOT_FOUND", ErrorType.NOT_FOUND, "Flight not found: " + flightId);
        }
        return seats.findByFlight(id, seatClass, page, size).stream().map(SeatView::from).toList();
    }

    private CustomException error(String code, ErrorType type, String message) {
        return new CustomException(code, type, message);
    }
}
