package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.in.SeatAssignmentUseCase;
import com.group3.flightsearch.domain.aggregate.Seat;
import com.group3.flightsearch.domain.model.FlightStatus;
import com.group3.flightsearch.domain.repository.FlightRepository;
import com.group3.flightsearch.domain.repository.SeatRepository;
import com.group3.flightsearch.domain.valueobject.FlightId;
import com.group3.flightsearch.domain.valueobject.SeatId;
import com.group3.flightsearch.domain.valueobject.TicketId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatAssignmentService implements SeatAssignmentUseCase {
    private final FlightRepository flights;
    private final SeatRepository seats;

    @Override
    public void assignOrChange(UUID flightId, UUID ticketId, UUID seatId) {
        var flightKey = new FlightId(flightId);
        var ticketKey = new TicketId(ticketId);
        var seatKey = new SeatId(seatId);
        // Same lock convention as seat creation: serialize inventory mutations per flight.
        var flight = flights.findByIdForUpdate(flightKey).orElseThrow(() -> missingFlight(flightId));
        var current = seats.findByAssignedTicketId(ticketKey);
        current.ifPresent(seat -> requireFlight(seat, flightKey));
        var target = seats.findById(seatKey).orElseThrow(() ->
                new CustomException("SEAT_NOT_FOUND", ErrorType.NOT_FOUND, "Seat not found: " + seatId));
        requireFlight(target, flightKey);
        // A retry of an already completed PUT is successful even after departure.
        if (current.isPresent() && current.get().getId().equals(seatKey)) {
            target.assignToTicket(ticketKey);
            return;
        }
        if ((flight.getStatus() != FlightStatus.SCHEDULED && flight.getStatus() != FlightStatus.DELAYED)
                || !flight.getDepartureTime().isAfter(Instant.now())) {
            throw new CustomException("FLIGHT_NOT_OPEN_FOR_SEATS", ErrorType.CONFLICT,
                    "Cannot assign seats on an inactive or departed flight");
        }
        // Validate the target before releasing the old seat. HELD/BLOCKED/unowned BOOKED stay unavailable.
        target.assignToTicket(ticketKey);
        current.ifPresent(previous -> {
            previous.releaseFromTicket(ticketKey);
            seats.save(previous); // Flush the unique ticket reference before assigning the replacement.
        });
        seats.save(target);
    }

    @Override
    public void release(UUID flightId, UUID ticketId) {
        var flightKey = new FlightId(flightId);
        var ticketKey = new TicketId(ticketId);
        flights.findByIdForUpdate(flightKey).orElseThrow(() -> missingFlight(flightId));
        // Missing assignment is already released; repeated DELETE returns success.
        seats.findByAssignedTicketId(ticketKey).ifPresent(seat -> {
            requireFlight(seat, flightKey);
            seat.releaseFromTicket(ticketKey);
            seats.save(seat);
        });
    }

    private void requireFlight(Seat seat, FlightId flightId) {
        if (!seat.getFlightId().equals(flightId)) {
            throw new CustomException("SEAT_FLIGHT_MISMATCH", ErrorType.CONFLICT,
                    "Seat or ticket assignment belongs to another flight");
        }
    }

    private CustomException missingFlight(UUID flightId) {
        return new CustomException("FLIGHT_NOT_FOUND", ErrorType.NOT_FOUND, "Flight not found: " + flightId);
    }
}
