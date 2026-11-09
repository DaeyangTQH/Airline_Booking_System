package com.group3.application.command;

import java.util.UUID;

public record CancelBookingCommand(
        UUID bookingId,
        UUID userId
) {
}
