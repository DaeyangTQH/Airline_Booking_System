package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.application.port.in.FlightUseCase;
import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.model.FareClass;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightUseCase useCase;

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

}
