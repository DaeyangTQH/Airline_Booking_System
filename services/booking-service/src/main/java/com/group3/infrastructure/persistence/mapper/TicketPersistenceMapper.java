package com.group3.infrastructure.persistence.mapper;

import com.group3.domain.aggregate.Ticket;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.SeatId;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.TicketNumber;
import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import com.group3.infrastructure.persistence.entity.TicketEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TicketPersistenceMapper {

    public TicketEntity toEntity(Ticket ticket, BookingPassengerEntity passenger){

        Objects.requireNonNull(ticket, "Ticket cannot be null");

        Objects.requireNonNull(passenger, "BookingPassengerEntity cannot be null");

        TicketEntity entity = new TicketEntity();

        entity.setId(ticket.getId().value());
        entity.setTicketNumber(ticket.getTicketNumber().value());
        entity.setPassenger(passenger);

        if(ticket.getSeatId() != null){
            entity.setSeatId(ticket.getSeatId().value());
        }

        entity.setStatus(ticket.getStatus());

        return entity;
    }

    public Ticket toDomain(TicketEntity entity){

        Objects.requireNonNull(entity, "TicketEntity cannot be null");

        SeatId seatId = null;

        if(entity.getSeatId() != null){
            seatId = new SeatId(entity.getSeatId());
        }

        return Ticket.restore(
                new TicketId(entity.getId()),
                new TicketNumber(entity.getTicketNumber()),
                new BookingPassengerId(entity.getPassenger().getId()),
                seatId,
                entity.getStatus()
        );

    }
}
