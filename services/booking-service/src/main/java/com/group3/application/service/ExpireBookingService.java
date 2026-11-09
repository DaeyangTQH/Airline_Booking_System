package com.group3.application.service;

import com.group3.application.port.in.ExpireBookingsUseCase;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ExpireBookingService implements ExpireBookingsUseCase {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public int expirePendingBookings(Instant now) {

        if(now == null){
            throw new InvalidDomainValueException(
                    "Current time must not be null"
            );
        }

        return bookingRepository
                .expirePendingBookings(now);
    }
}
