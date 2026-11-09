package com.group3.application.command;

import java.util.UUID;

public record RemoveSeatCommand(
        UUID ticketId,
        UUID userId
) {
}
