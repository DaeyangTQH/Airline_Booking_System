package com.group3.flightsearch.application.query;

import com.group3.flightsearch.domain.aggregate.Seat;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.model.SeatStatus;
import java.util.UUID;

// Public catalog data deliberately excludes the booking owner.
public record SeatView(UUID id, UUID flightId, String seatNumber, FareClass seatClass, SeatStatus status) {
    public static SeatView from(Seat seat) {
        return new SeatView(seat.getId().value(), seat.getFlightId().value(), seat.getNumber().value(),
                seat.getSeatClass(), seat.getStatus());
    }
}
