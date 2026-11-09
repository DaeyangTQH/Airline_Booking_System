package com.group3.application.integration;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
        UUID eventId,
        UUID userId,
        String recipientEmail,
        NotificationEventType type,
        String title,
        String message,
        UUID bookingId,
        UUID ticketId,
        Instant occurredAt
) {
}

