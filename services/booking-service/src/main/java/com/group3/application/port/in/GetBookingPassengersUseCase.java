package com.group3.application.port.in;

import com.group3.application.query.BookingPassengerView;

import java.util.List;
import java.util.UUID;

public interface GetBookingPassengersUseCase {

    List<BookingPassengerView> getPassengers(
            UUID bookingId,
            UUID userId
    );
}
