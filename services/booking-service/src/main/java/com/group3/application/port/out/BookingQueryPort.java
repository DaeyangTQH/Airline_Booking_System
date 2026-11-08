package com.group3.application.port.out;

import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingSummaryView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingQueryPort {

    Optional<BookingDetailView> findDetailByIdAndUserId(
            UUID bookingId,
            UUID userId
    );

    Optional<BookingDetailView> findDetailByReferenceAndUserId(
            String bookingReference,
            UUID userId
    );

    List<BookingSummaryView> findSummariesByUserId(UUID userId);
}
