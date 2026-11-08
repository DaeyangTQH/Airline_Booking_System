package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.api.dto.*;
import com.group3.flightsearch.application.command.*;
import com.group3.flightsearch.application.port.in.FlightUseCase;
import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.model.FareClass;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightUseCase useCase;

    @PostMapping
    public ResponseEntity<FlightView> create(@Valid @RequestBody CreateFlightRequest request) {
        var result = useCase.create(new CreateFlightCommand(request.flightCode(), request.originCode(),
                request.destinationCode(), request.departureTime(), request.arrivalTime(), request.aircraftId()));
        return ResponseEntity.created(URI.create("/api/v1/flights/" + result.id())).body(result);
    }

    @GetMapping
    public List<FlightView> search(
            @RequestParam(name = "origin", required = false) String origin,
            @RequestParam(name = "destination", required = false) String destination,
            @RequestParam(name = "departureDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "fareClass", required = false) FareClass fareClass,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return useCase.search(origin, destination, date, fareClass, quantity, page, size);
    }

    @GetMapping("/{flightId}")
    public FlightView getById(@PathVariable("flightId") UUID flightId) {
        return useCase.getById(flightId);
    }

    @PutMapping("/{flightId}/schedule")
    public FlightView updateSchedule(@PathVariable("flightId") UUID flightId,
                                    @Valid @RequestBody FlightScheduleRequest request) {
        return useCase.updateSchedule(new UpdateFlightScheduleCommand(flightId, request.departureTime(), request.arrivalTime()));
    }

    @GetMapping("/{flightId}/fares")
    public List<FareView> getFares(@PathVariable("flightId") UUID flightId) {
        return useCase.getFares(flightId);
    }

    @GetMapping("/{flightId}/fares/{fareId}")
    public FareView getFare(@PathVariable("flightId") UUID flightId, @PathVariable("fareId") UUID fareId) {
        return useCase.getFare(flightId, fareId);
    }

    @PostMapping("/{flightId}/fares")
    public ResponseEntity<FareView> addFare(@PathVariable("flightId") UUID flightId,
                                           @Valid @RequestBody CreateFareRequest request) {
        var result = useCase.addFare(new CreateFareCommand(flightId, request.fareClass(), request.basePrice(),
                request.currency(), request.validFrom(), request.validTo()));
        return ResponseEntity.created(URI.create("/api/v1/flights/" + flightId + "/fares/" + result.id())).body(result);
    }

    @PutMapping("/{flightId}/fares/{fareId}")
    public FareView updateFare(@PathVariable("flightId") UUID flightId, @PathVariable("fareId") UUID fareId,
                               @Valid @RequestBody UpdateFareRequest request) {
        return useCase.updateFare(new UpdateFareCommand(flightId, fareId, request.basePrice(),
                request.currency(), request.validFrom(), request.validTo()));
    }
}
