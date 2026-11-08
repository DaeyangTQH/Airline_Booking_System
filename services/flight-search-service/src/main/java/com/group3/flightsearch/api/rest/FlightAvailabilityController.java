package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.application.port.in.GetFlightAvailabilityUseCase;
import com.group3.flightsearch.application.query.FlightAvailabilityView;
import com.group3.flightsearch.domain.model.FareClass;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flights/{flightId}/availability")
@RequiredArgsConstructor
public class FlightAvailabilityController {
    private final GetFlightAvailabilityUseCase useCase;

    @GetMapping
    public List<FlightAvailabilityView> getByFlight(@PathVariable("flightId") UUID flightId) {
        return useCase.getByFlight(flightId);
    }

    @GetMapping("/{fareClass}")
    public FlightAvailabilityView getByClass(@PathVariable("flightId") UUID flightId,
                                            @PathVariable("fareClass") FareClass fareClass) {
        return useCase.getByClass(flightId, fareClass);
    }
}
