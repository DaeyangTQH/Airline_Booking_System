package com.group3.domain.repository;

import com.group3.domain.aggregate.Ticket;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.TicketNumber;

import java.util.Optional;

public interface TicketRepository {

    Ticket save(Ticket ticket);

    Optional<Ticket> findByIdForUpdate(TicketId id);

    boolean existsByNumber(TicketNumber number);
}
