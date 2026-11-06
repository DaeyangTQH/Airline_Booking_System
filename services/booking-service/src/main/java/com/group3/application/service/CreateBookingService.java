package com.group3.application.service;

import com.group3.application.command.CreateBookingCommand;
import com.group3.application.exception.BookingReferenceGenerationException;
import com.group3.application.exception.FlightNotBookableException;
import com.group3.application.exception.InsufficientSeatAvailabilityException;
import com.group3.application.integration.FlightFareQuote;
import com.group3.application.port.in.CreateBookingUseCase;
import com.group3.application.port.out.BookingReferenceGenerator;
import com.group3.application.port.out.FlightSearchPort;
import com.group3.application.result.CreateBookingResult;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.ContactInfo;
import com.group3.domain.vo.FareClass;
import com.group3.domain.vo.FlightId;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.TicketQuantity;
import com.group3.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateBookingService implements CreateBookingUseCase {

    private static final Duration BOOKING_EXPIRATION =
            Duration.ofMinutes(15);

    private static final int
            MAX_REFERENCE_GENERATION_ATTEMPTS = 20;

    private final FlightSearchPort flightSearchPort;
    private final BookingRepository bookingRepository;
    private final BookingReferenceGenerator bookingReferenceGenerator;

    @Override
    public CreateBookingResult create(CreateBookingCommand command) {

        if(command == null) {
            throw new InvalidDomainValueException(
                    "Create booking command must not be null"
            );
        }

        if(command.fareClass() == null){
            throw new InvalidDomainValueException(
                    "Fare class must not be null"
            );
        }

        UserId userId = new UserId(command.userId());
        FlightId flightId = new FlightId(command.flightId());
        TicketQuantity ticketQuantity = new TicketQuantity(command.ticketQuantity());
        ContactInfo contactInfo = new ContactInfo(command.phone(), command.email());
        FlightFareQuote fareQuote =
                flightSearchPort.getFareQuote(
                        flightId.value(),
                        command.fareClass(),
                        ticketQuantity.value()
                );

        validateFareQuote(fareQuote, flightId, command.fareClass(), ticketQuantity);

        Money unitPrice = new Money(fareQuote.unitPrice(), fareQuote.currency());

        Instant createdAt = Instant.now();

        Instant expiresAt = createdAt.plus(BOOKING_EXPIRATION);

        Booking booking = Booking.create(
                BookingId.generate(),
                generateUniqueReference(),
                userId,
                flightId,
                command.fareClass(),
                ticketQuantity,
                unitPrice,
                contactInfo,
                createdAt,
                expiresAt
        );

        Booking savedBooking = bookingRepository.save(booking);

        return toResult(savedBooking);
    }

    private void validateFareQuote(
            FlightFareQuote fareQuote,
            FlightId flightId,
            FareClass fareClass,
            TicketQuantity ticketQuantity
    ){
        if(fareQuote == null) {
            throw new IllegalStateException(
                    "Flight Search returned no fare quote"
            );
        }

        if(!flightId.value().equals(fareQuote.flightId())) {
            throw new IllegalStateException(
                    "Flight Search returned a quote for another flight"
            );
        }

        if(fareClass != fareQuote.fareClass()){
            throw new IllegalStateException(
                    "Flight Search returned a quote for another fare class"
            );
        }

        if(!fareQuote.bookable()){
            throw new FlightNotBookableException(
                    "Flight is not available for booking"
            );
        }

        if(fareQuote.availableSeats() < ticketQuantity.value()) {
            throw new InsufficientSeatAvailabilityException(
                    "Requested" + ticketQuantity.value() + "seats but only" + fareQuote.availableSeats() + "seats are available"
            );
        }
    }

    private BookingReference generateUniqueReference(){
        for (int attempt = 0; attempt < MAX_REFERENCE_GENERATION_ATTEMPTS; attempt ++){
            BookingReference reference = bookingReferenceGenerator.generate();

            boolean alreadyExists = bookingRepository.existsByReference(reference);

            if(!alreadyExists) {
                return reference;
            }
        }

        throw new BookingReferenceGenerationException(
                "Could not generate a unique booking reference"
        );
    }

    private CreateBookingResult toResult(Booking booking){
        return new CreateBookingResult(
                booking.getId().value(),
                booking.getReference().value(),
                booking.getStatus(),
                booking.getFlightId().value(),
                booking.getFareClass(),
                booking.getTicketQuantity().value(),
                booking.getUnitPrice().amount(),
                booking.getTotalAmount().amount(),
                booking.getUnitPrice().currency(),
                booking.getExpiresAt()
        );
    }
}
