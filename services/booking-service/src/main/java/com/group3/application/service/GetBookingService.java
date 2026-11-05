package com.group3.application.service;

import com.group3.application.port.in.GetBookingUseCase;
import com.group3.application.port.out.BookingQueryPort;
import com.group3.application.query.BookingDetailView;
import com.group3.domain.exception.BookingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBookingService implements GetBookingUseCase {

    private final BookingQueryPort bookingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public BookingDetailView getById(UUID bookingId) {
        return bookingQueryPort.findDetailById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with ID: " + bookingId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailView getByReference(String bookingReference) {

        return bookingQueryPort.findDetailByReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with reference: " + bookingReference
                ));
    }
}
