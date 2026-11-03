package com.group3.application.service;

import com.group3.application.port.in.GetBookingUseCase;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBookingService implements GetBookingUseCase {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public Booking getById(BookingId bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with ID: " + bookingId.value()
                ));
    }
}
