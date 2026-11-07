package com.group3.application.command;

import java.util.UUID;

public record InitiatePaymentCommand(
        UUID bookingId,
        UUID userId
) {
}
