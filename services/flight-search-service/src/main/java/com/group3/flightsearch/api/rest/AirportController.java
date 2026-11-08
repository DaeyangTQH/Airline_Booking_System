package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.api.dto.CreateAirportRequest;
import com.group3.flightsearch.api.dto.UpdateAirportRequest;
import com.group3.flightsearch.application.command.CreateAirportCommand;
import com.group3.flightsearch.application.command.UpdateAirportCommand;
import com.group3.flightsearch.application.port.in.AirportUseCase;
import com.group3.flightsearch.application.query.AirportView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
public class AirportController {
    private final AirportUseCase useCase;

    @PostMapping
    public ResponseEntity<AirportView> create(@Valid @RequestBody CreateAirportRequest request) {
        AirportView result = useCase.create(new CreateAirportCommand(
                request.code(), request.name(), request.city(), request.country()));
        return ResponseEntity.created(URI.create("/api/v1/airports/" + result.id())).body(result);
    }

    @GetMapping("/{airportId}")
    public AirportView getById(@PathVariable("airportId") UUID airportId) {
        return useCase.getById(airportId);
    }

    @GetMapping
    public List<AirportView> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "20") int size) {
        return useCase.list(page, size);
    }

    @PutMapping("/{airportId}")
    public AirportView update(@PathVariable("airportId") UUID airportId,
                             @Valid @RequestBody UpdateAirportRequest request) {
        return useCase.update(new UpdateAirportCommand(
                airportId, request.name(), request.city(), request.country()));
    }
}
