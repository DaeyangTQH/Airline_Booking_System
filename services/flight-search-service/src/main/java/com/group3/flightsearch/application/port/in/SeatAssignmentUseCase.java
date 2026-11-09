package com.group3.flightsearch.application.port.in;

import java.util.UUID;

public interface SeatAssignmentUseCase {
    void assignOrChange(UUID flightId, UUID ticketId, UUID seatId);
    void release(UUID flightId, UUID ticketId);
}
