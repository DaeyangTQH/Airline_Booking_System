package com.group3.application.service;

import com.group3.application.command.AssignSeatCommand;
import com.group3.application.exception.BookingAccessDeniedException;
import com.group3.application.port.in.AssignSeatUseCase;
import com.group3.application.port.out.FlightSeatPort;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.aggregate.Ticket;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.exception.TicketNotFoundException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.repository.TicketRepository;
import com.group3.domain.vo.SeatId;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignSeatService
        implements AssignSeatUseCase {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final FlightSeatPort flightSeatPort;

    @Override
    @Transactional
    public void assignSeat(
            AssignSeatCommand command
    ) {
        if (command == null) {
            throw new InvalidDomainValueException(
                    "Assign seat command must not be null"
            );
        }

        TicketId ticketId =
                new TicketId(
                        command.ticketId()
                );

        UserId userId =
                new UserId(
                        command.userId()
                );

        SeatId seatId =
                new SeatId(
                        command.seatId()
                );

        Ticket ticket = ticketRepository
                .findByIdForUpdate(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId.value()
                        )
                );

        Booking booking = bookingRepository
                .findByPassengerId(
                        ticket.getBookingPassengerId()
                )
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking for ticket was not found"
                        )
                );

        verifyOwnership(
                booking,
                userId
        );

        ticket.assignSeat(seatId);

        flightSeatPort.assignOrChangeSeat(
                booking.getFlightId(),
                ticketId,
                seatId
        );

        ticketRepository.save(
                ticket
        );
    }

    private void verifyOwnership(
            Booking booking,
            UserId userId
    ) {
        if (!booking.getUserId().equals(userId)) {
            throw new BookingAccessDeniedException(
                    "You do not have permission to modify this ticket"
            );
        }
    }
}
