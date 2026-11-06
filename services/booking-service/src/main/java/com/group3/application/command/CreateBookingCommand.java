package com.group3.application.command;

import com.group3.domain.vo.FareClass;

import java.util.UUID;

public record CreateBookingCommand(
        UUID userId,
        UUID flightId,
        FareClass fareClass,
        int ticketQuantity,
        String phone,
        String email
) {
}
