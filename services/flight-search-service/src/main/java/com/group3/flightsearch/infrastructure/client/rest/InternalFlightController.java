package com.group3.flightsearch.infrastructure.client.rest;

import com.group3.flightsearch.application.port.in.GetFareQuoteUseCase;
import com.group3.flightsearch.application.port.in.SeatAssignmentUseCase;
import com.group3.flightsearch.infrastructure.client.dto.AssignSeatRequest;
import com.group3.flightsearch.infrastructure.client.dto.FareQuoteRequest;
import com.group3.flightsearch.infrastructure.client.dto.FareQuoteResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.group3.flightsearch.infrastructure.security.InternalApiKeyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/flights")
@RequiredArgsConstructor
public class InternalFlightController {
    private final GetFareQuoteUseCase useCase;
    private final InternalApiKeyValidator internalApiKeyValidator;
    private final SeatAssignmentUseCase seatAssignments;

    @GetMapping("/{flightId}/fare-quote")
    public FareQuoteResponse getQuote(
            @PathVariable("flightId") UUID flightId,
            @Valid @ModelAttribute FareQuoteRequest request,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String apiKey) {
        internalApiKeyValidator.validate(apiKey);
        return FareQuoteResponse.from(useCase.getQuote(flightId, request.fareClass(), request.quantity()));
    }

    @PutMapping("/{flightId}/seat-assignments/{ticketId}")
    public ResponseEntity<Void> assignOrChangeSeat(
            @PathVariable("flightId") UUID flightId,
            @PathVariable("ticketId") UUID ticketId,
            @Valid @RequestBody AssignSeatRequest request,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String apiKey) {
        internalApiKeyValidator.validate(apiKey);
        seatAssignments.assignOrChange(flightId, ticketId, request.seatId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{flightId}/seat-assignments/{ticketId}")
    public ResponseEntity<Void> releaseSeat(
            @PathVariable("flightId") UUID flightId,
            @PathVariable("ticketId") UUID ticketId,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String apiKey) {
        internalApiKeyValidator.validate(apiKey);
        seatAssignments.release(flightId, ticketId);
        return ResponseEntity.noContent().build();
    }
}
