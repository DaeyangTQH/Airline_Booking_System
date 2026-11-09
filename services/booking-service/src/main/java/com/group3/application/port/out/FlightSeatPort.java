package com.group3.application.port.out;

import com.group3.domain.vo.FlightId;
import com.group3.domain.vo.SeatId;
import com.group3.domain.vo.TicketId;

public interface FlightSeatPort {

    void assignOrChangeSeat(
            FlightId flightId,
            TicketId ticketId,
            SeatId seatId
    );

    void releaseSeat(
            FlightId flightId,
            TicketId ticketId
    );
}
