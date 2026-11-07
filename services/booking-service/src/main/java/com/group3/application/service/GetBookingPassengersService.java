package com.group3.application.service;

import com.group3.application.exception.BookingAccessDeniedException;
import com.group3.application.port.in.GetBookingPassengersUseCase;
import com.group3.application.port.out.BookingQueryPort;
import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingPassengerView;
import com.group3.domain.exception.BookingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBookingPassengersService implements GetBookingPassengersUseCase {

    private final BookingQueryPort bookingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<BookingPassengerView> getPassengers(UUID bookingId, UUID userId) {

        BookingDetailView booking =
                bookingQueryPort
                        .findDetailById(bookingId)
                        .orElseThrow(() -> new BookingNotFoundException(
                                "Booking not found with ID: " + bookingId
                        ));

        if(!booking.userId().equals(userId)) {
            throw new BookingAccessDeniedException(
                    "You do not have permission to view passengers of this booking"
            );
        }
        return booking.passengers();
    }
}
