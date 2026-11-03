package com.group3.application.port.in;

import com.group3.domain.aggregate.Booking;
import com.group3.domain.vo.BookingId;

public interface GetBookingUseCase {

    Booking getById(BookingId bookingId);
}
