package com.group3.application.query;

import com.group3.domain.aggregate.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketDetailView(
        UUID id,
        String ticketNumber,
        UUID bookingId,
        UUID bookingPassengerId,
        UUID seatId,
        TicketStatus status,
        Instant issuedAt
) {
}
