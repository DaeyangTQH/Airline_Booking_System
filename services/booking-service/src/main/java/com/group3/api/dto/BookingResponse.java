package com.group3.api.dto;

import com.group3.domain.aggregate.BookingStatus;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.FareClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String bookingReference,
        UUID userId,
        UUID flightId,
        FareClass fareClass,
        int ticketQuantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        CurrencyCode currency,
        String phone,
        String email,
        BookingStatus status,
        Instant createdAt,
        Instant expiresAt,
        List<BookingPassengerResponse> passengers
) {
}
