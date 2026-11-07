package com.group3.application.result;

import com.group3.domain.aggregate.TicketStatus;

import java.util.UUID;

public record AddPassengerResult(
        UUID passengerId,
        TicketResult ticket
) {

    public record TicketResult(
            UUID ticketId,
            String ticketNumber,
            UUID seatId,
            TicketStatus status
    ){
    }
}
