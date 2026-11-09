package com.group3.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTicketSeatRequest(

        @NotNull(message = "Seat ID must not be null")
        UUID seatId
) {
}

