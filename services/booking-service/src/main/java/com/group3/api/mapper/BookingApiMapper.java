package com.group3.api.mapper;

import com.group3.api.dto.BookingPassengerResponse;
import com.group3.api.dto.BookingResponse;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.aggregate.BookingPassenger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingApiMapper {

    public BookingResponse toResponse(Booking booking){
        List<BookingPassengerResponse> passengers =
                booking.getPassengers()
                        .stream()
                        .map(this::toPassengerResponse)
                        .toList();

        return new BookingResponse(
                booking.getId().value(),
                booking.getReference().value(),
                booking.getUserId().value(),
                booking.getFlightId().value(),
                booking.getFareClass(),
                booking.getTicketQuantity().value(),
                booking.getUnitPrice().amount(),
                booking.getTotalAmount().amount(),
                booking.getUnitPrice().currency(),
                booking.getContactInfo().phone(),
                booking.getContactInfo().email(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getExpiresAt(),
                passengers
        );
    }

    private BookingPassengerResponse toPassengerResponse(BookingPassenger passenger){
        return new BookingPassengerResponse(
                passenger.getId().value(),

                passenger.getPassengerProfileId() == null
                ? null
                : passenger.getPassengerProfileId().value(),

                passenger.getFullName().value(),
                passenger.getPassportNumber().value(),
                passenger.getDateOfBirth(),

                passenger.getContactInfo() == null
                ? null
                : passenger.getContactInfo().phone(),

                passenger.getContactInfo() == null
                ? null
                : passenger.getContactInfo().email()
        );
    }
}
