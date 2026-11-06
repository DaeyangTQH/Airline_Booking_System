package com.group3.application.result;

import com.group3.domain.aggregate.BookingStatus;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.FareClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateBookingResult(
        UUID bookingId,
        String bookingReference,
        BookingStatus status,
        UUID flightId,
        FareClass fareClass,
        int ticketQuantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        CurrencyCode currency,
        Instant expiresAt
) {
}
