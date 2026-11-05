package com.group3.application.port.out;

import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingSumaryView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingQueryPort {

    Optional<BookingDetailView> findDetailById(UUID bookingId);

    Optional<BookingDetailView> findDetailByReference(String bookingReference);

    List<BookingSumaryView> findSumariesByUserId(UUID userId);
}
