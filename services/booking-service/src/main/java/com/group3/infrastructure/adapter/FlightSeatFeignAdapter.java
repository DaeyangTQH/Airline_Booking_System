package com.group3.infrastructure.adapter;

import com.group3.application.exception.FlightSeatCommunicationException;
import com.group3.application.exception.SeatNotAvailableException;
import com.group3.application.exception.SeatNotFoundException;
import com.group3.application.port.out.FlightSeatPort;
import com.group3.domain.vo.FlightId;
import com.group3.domain.vo.SeatId;
import com.group3.domain.vo.TicketId;
import com.group3.infrastructure.client.flight.FlightSeatFeignClient;
import com.group3.infrastructure.client.flight.dto.AssignSeatRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlightSeatFeignAdapter implements FlightSeatPort {

    private final FlightSeatFeignClient flightSeatFeignClient;

    @Override
    public void assignOrChangeSeat(
            FlightId flightId,
            TicketId ticketId,
            SeatId seatId
    ) {
        AssignSeatRequest request =
                new AssignSeatRequest(seatId.value());

        try {
            flightSeatFeignClient
                    .putSeatAssignment(
                            flightId.value(),
                            ticketId.value(),
                            request
                    );

        } catch (FeignException exception) {
            throw translateException(exception);
        }
    }

    @Override
    public void releaseSeat(
            FlightId flightId,
            TicketId ticketId
    ) {
        try {
            flightSeatFeignClient
                    .releaseSeatAssignment(
                            flightId.value(),
                            ticketId.value()
                    );

        } catch (FeignException exception) {
            throw translateException(exception);
        }
    }

    private RuntimeException translateException(
            FeignException exception
    ) {
        return switch (exception.status()) {
            case 404 -> new SeatNotFoundException(
                    "Flight or seat was not found"
            );

            case 409 -> new SeatNotAvailableException(
                    "Seat is not available"
            );

            default ->
                    new FlightSeatCommunicationException(
                            "Could not communicate with Flight Search Service"
                    );
        };
    }
}
