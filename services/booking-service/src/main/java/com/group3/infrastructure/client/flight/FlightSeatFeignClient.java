package com.group3.infrastructure.client.flight;

import com.group3.infrastructure.client.flight.dto.AssignSeatRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "flight-search-service",
        contextId = "flightSeatFeignClient",
        path = "/internal/v1/flights"
)
public interface FlightSeatFeignClient {

    @PutMapping("/{flightId}/seat-assignments/{ticketId}")
    void putSeatAssignment(
            @PathVariable("flightId") UUID flightId,

            @PathVariable("ticketId") UUID ticketId,

            @RequestBody AssignSeatRequest request
    );

    @DeleteMapping("/{flightId}/seat-assignments/{ticketId}")
    void releaseSeatAssignment(
            @PathVariable("flightId") UUID flightId,

            @PathVariable("ticketId") UUID ticketId
    );
}
