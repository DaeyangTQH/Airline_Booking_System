package com.group3.application.service;

import com.group3.application.port.in.GetMyBookingsUseCase;
import com.group3.application.port.out.BookingQueryPort;
import com.group3.application.query.BookingSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyBookingsService implements GetMyBookingsUseCase {

    private final BookingQueryPort bookingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<BookingSummaryView> getByUserId(UUID userId) {
        return bookingQueryPort
                .findSummariesByUserId(userId);
    }
}
