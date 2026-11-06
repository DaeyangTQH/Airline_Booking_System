package com.group3.application.result;

import com.group3.domain.aggregate.TicketStatus;

import java.util.UUID;

public record AddPassengerResult(
        UUID passengerId,
        TicketReSult ticket
) {

    public record TicketReSult(
            UUID ticketId,
            String ticketNumber,
            UUID seatId,
            TicketStatus status
    ){
    }
}
