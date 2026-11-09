package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.application.port.in.SeatUseCase;
import com.group3.flightsearch.application.query.SeatView;
import com.group3.flightsearch.domain.model.FareClass;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SeatController {
    private final SeatUseCase useCase;

    @GetMapping("/api/v1/flights/{flightId}/seats")
    public List<SeatView> list(@PathVariable("flightId") UUID flightId,
                              @RequestParam(name = "seatClass", required = false) FareClass seatClass,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "20") int size) {
        return useCase.list(flightId, seatClass, page, size);
    }

}
