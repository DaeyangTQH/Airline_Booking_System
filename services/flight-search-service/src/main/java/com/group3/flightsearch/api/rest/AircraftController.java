package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.api.dto.AircraftRequest;
import com.group3.flightsearch.application.command.CreateAircraftCommand;
import com.group3.flightsearch.application.command.UpdateAircraftCommand;
import com.group3.flightsearch.application.port.in.AircraftUseCase;
import com.group3.flightsearch.application.query.AircraftView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aircrafts")
@RequiredArgsConstructor
public class AircraftController {
    private final AircraftUseCase useCase;

    @PostMapping
    public ResponseEntity<AircraftView> create(@Valid @RequestBody AircraftRequest request) {
        AircraftView result = useCase.create(new CreateAircraftCommand(request.model(), request.totalSeats()));
        return ResponseEntity.created(URI.create("/api/v1/aircrafts/" + result.id())).body(result);
    }

    @GetMapping("/{aircraftId}")
    public AircraftView getById(@PathVariable("aircraftId") UUID aircraftId) {
        return useCase.getById(aircraftId);
    }

    @GetMapping
    public List<AircraftView> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "20") int size) {
        return useCase.list(page, size);
    }

    @PutMapping("/{aircraftId}")
    public AircraftView update(@PathVariable("aircraftId") UUID aircraftId,
                              @Valid @RequestBody AircraftRequest request) {
        return useCase.update(new UpdateAircraftCommand(aircraftId, request.model(), request.totalSeats()));
    }
}
