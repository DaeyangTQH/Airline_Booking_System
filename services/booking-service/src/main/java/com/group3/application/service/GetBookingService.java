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
    public BookingDetailView getById(UUID bookingId, UUID userId) {
        return bookingQueryPort
                .findDetailByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with ID: " + bookingId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailView getByReference(
            String bookingReference,
            UUID userId
    ) {

        return bookingQueryPort
                .findDetailByReferenceAndUserId(bookingReference, userId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with reference: " + bookingReference
                ));
    }
}
