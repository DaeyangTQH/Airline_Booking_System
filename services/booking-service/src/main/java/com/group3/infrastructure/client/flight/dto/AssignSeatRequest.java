package com.group3.infrastructure.client.flight.dto;

import java.util.UUID;

public record AssignSeatRequest(
        UUID seatId
) {
}

