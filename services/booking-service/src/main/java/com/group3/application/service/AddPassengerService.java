package com.group3.application.service;

import com.group3.application.command.AddPassengerCommand;
import com.group3.application.exception.BookingAccessDeniedException;
import com.group3.application.exception.TicketNumberGenerationException;
import com.group3.application.port.in.AddPassengerUseCase;
import com.group3.application.port.out.TicketNumberGenerator;
import com.group3.application.result.AddPassengerResult;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.aggregate.BookingPassenger;
import com.group3.domain.aggregate.Ticket;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.repository.TicketRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.ContactInfo;
import com.group3.domain.vo.PassengerName;
import com.group3.domain.vo.PassengerProfileId;
import com.group3.domain.vo.PassportNumber;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.TicketNumber;
import com.group3.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddPassengerService implements AddPassengerUseCase {

    private static final int MAX_TICKET_NUMBER_ATTEMPTS = 20;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TicketNumberGenerator ticketNumberGenerator;

    @Override
    @Transactional
    public AddPassengerResult addPassenger(AddPassengerCommand command) {

        if(command == null) {
            throw new InvalidDomainValueException(
                    "Add passenger command must not be null"
            );
        }

        BookingId bookingId =
                new BookingId(command.bookingId());

        UserId currentUserId =
                new UserId(command.userId());

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with ID: " + bookingId.value())
                );

        verifyOwnership(booking, currentUserId);

        BookingPassengerId passengerId =
                BookingPassengerId.generate();

        PassengerProfileId passengerProfileId =
                command.passengerProfileId() == null
                    ? null
                    : new PassengerProfileId(command.passengerProfileId());

        BookingPassenger passenger =
                BookingPassenger.create(
                        passengerId,
                        passengerProfileId,
                        new PassengerName(command.fullName()),
                        new PassportNumber(command.passportNumber()),
                        command.dateOfBirth(),
                        new ContactInfo(command.phone(), command.email())
                );

        booking.addPassenger(passenger);

        Ticket ticket = Ticket.issue(
                TicketId.generate(),
                generateUniqueTicketNumber(),
                passengerId
        );

        bookingRepository.save(booking);

        Ticket savedTicket = ticketRepository.save(ticket);

        return toResult(
                passenger,
                savedTicket
        );
    }

    private void verifyOwnership(Booking booking, UserId currentUserId){

        if(!booking.getUserId()
                .equals(currentUserId)){
            throw new BookingAccessDeniedException(
                    "You do not have permission to modify this booking"
            );
        }
    }

    private TicketNumber generateUniqueTicketNumber(){

        for(int attempt = 0; attempt < MAX_TICKET_NUMBER_ATTEMPTS; attempt ++){

            TicketNumber ticketNumber =
                    ticketNumberGenerator.generate();

            boolean alreadyExists = ticketRepository.existsByNumber(ticketNumber);

            if(!alreadyExists){
                return ticketNumber;
            }
        }

        throw new TicketNumberGenerationException(
                "Could not generate a unique ticket number after "
                + MAX_TICKET_NUMBER_ATTEMPTS + " attempts"
        );
    }

    private AddPassengerResult toResult(
            BookingPassenger passenger,
            Ticket ticket
    ){
        return new AddPassengerResult(
                passenger.getId().value(),

                new AddPassengerResult.TicketResult(
                        ticket.getId().value(),
                        ticket.getTicketNumber().value(),
                        ticket.getSeatId() == null
                                ? null
                                : ticket.getSeatId().value(),
                        ticket.getStatus()
                )
        );
    }
}
