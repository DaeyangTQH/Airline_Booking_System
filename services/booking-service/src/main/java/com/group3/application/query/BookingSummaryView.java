package com.group3.application.query;

import com.group3.domain.aggregate.BookingStatus;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.FareClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingSummaryView(
        UUID id,
        String bookingReference,
        UUID flightId,
        FareClass fareClass,
        int ticketQuantity,
        BigDecimal totalAmount,
        CurrencyCode currency,
        BookingStatus status,
        Instant createdAt,
        Instant expiresAt
) {
}
