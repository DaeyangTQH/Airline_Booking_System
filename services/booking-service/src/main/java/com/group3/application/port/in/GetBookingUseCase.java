package com.group3.application.port.in;

import com.group3.application.query.BookingDetailView;

import java.util.UUID;

public interface GetBookingUseCase {

    BookingDetailView getById(UUID bookingId);

    BookingDetailView getByReference(String bookingReference);
}
