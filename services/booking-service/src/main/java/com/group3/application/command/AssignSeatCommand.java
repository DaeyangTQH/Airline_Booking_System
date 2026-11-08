package com.group3.application.command;

import java.util.UUID;

public record AssignSeatCommand(
        UUID ticketId,
        UUID userId,
        UUID seatId
) {
}
