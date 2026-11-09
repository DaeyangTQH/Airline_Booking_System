package com.group3.flightsearch.domain.repository;

import com.group3.flightsearch.domain.aggregate.Seat;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.valueobject.*;
import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    Seat save(Seat seat);
    Optional<Seat> findById(SeatId id);
    Optional<Seat> findByAssignedTicketId(TicketId ticketId);
    List<Seat> findByFlight(FlightId flightId, FareClass seatClass, int page, int size);
}
