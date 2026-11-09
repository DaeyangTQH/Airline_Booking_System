package com.group3.flightsearch.infrastructure.persistence.mapper;

import com.group3.flightsearch.domain.aggregate.Seat;
import com.group3.flightsearch.domain.valueobject.*;
import com.group3.flightsearch.infrastructure.persistence.entity.SeatJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SeatPersistenceMapper {
    public SeatJpaEntity toEntity(Seat seat) {
        var entity = new SeatJpaEntity();
        entity.setId(seat.getId().value());
        entity.setFlightId(seat.getFlightId().value());
        entity.setSeatNumber(seat.getNumber().value());
        entity.setSeatClass(seat.getSeatClass());
        entity.setStatus(seat.getStatus());
        entity.setHeldByBookingId(seat.getHeldByBookingId() == null ? null : seat.getHeldByBookingId().value());
        entity.setHoldExpiresAt(seat.getHoldExpiresAt());
        entity.setAssignedTicketId(seat.getAssignedTicketId() == null ? null : seat.getAssignedTicketId().value());
        return entity;
    }

    public Seat toDomain(SeatJpaEntity entity) {
        return Seat.restore(new SeatId(entity.getId()), new FlightId(entity.getFlightId()),
                new SeatNumber(entity.getSeatNumber()), entity.getSeatClass(), entity.getStatus(),
                entity.getHeldByBookingId() == null ? null : new BookingId(entity.getHeldByBookingId()),
                entity.getHoldExpiresAt(),
                entity.getAssignedTicketId() == null ? null : new TicketId(entity.getAssignedTicketId()));
    }
}
