package com.group3.application.service;

import com.group3.application.port.in.GetMyBookingUseCase;
import com.group3.application.port.out.BookingQueryPort;
import com.group3.application.query.BookingSumaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyBookingService implements GetMyBookingUseCase {

    private final BookingQueryPort bookingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<BookingSumaryView> getByUserId(UUID userId) {
        return bookingQueryPort
                .findSumariesByUserId(userId);
    }
}
