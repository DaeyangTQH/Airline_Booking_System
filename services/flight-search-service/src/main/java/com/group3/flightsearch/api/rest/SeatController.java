package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.api.dto.CreateSeatRequest;
import com.group3.flightsearch.application.command.CreateSeatCommand;
import com.group3.flightsearch.application.port.in.SeatUseCase;
import com.group3.flightsearch.application.query.SeatView;
import com.group3.flightsearch.domain.model.FareClass;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SeatController {
    private final SeatUseCase useCase;

    @PostMapping("/api/v1/flights/{flightId}/seats")
    public ResponseEntity<SeatView> create(@PathVariable("flightId") UUID flightId,
                                          @Valid @RequestBody CreateSeatRequest request) {
        var result = useCase.create(new CreateSeatCommand(flightId, request.seatNumber(), request.seatClass()));
        return ResponseEntity.created(URI.create("/api/v1/seats/" + result.id())).body(result);
    }

    @GetMapping("/api/v1/flights/{flightId}/seats")
    public List<SeatView> list(@PathVariable("flightId") UUID flightId,
                              @RequestParam(name = "seatClass", required = false) FareClass seatClass,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "20") int size) {
        return useCase.list(flightId, seatClass, page, size);
    }

    @GetMapping("/api/v1/seats/{seatId}")
    public SeatView getById(@PathVariable("seatId") UUID seatId) {
        return useCase.getById(seatId);
    }
}
