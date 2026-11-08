package com.group3.domain.repository;

import com.group3.domain.aggregate.Ticket;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.TicketNumber;

import java.util.Optional;

public interface TicketRepository {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(TicketId id);

    Optional<Ticket> findByIdForUpdate(TicketId id);

    Optional<Ticket> findByNumber(TicketNumber number);

    Optional<Ticket> findByPassengerId(BookingPassengerId passengerId);

    boolean existsByPassengerId(BookingPassengerId passengerId);

    boolean existsByNumber(TicketNumber number);
}
