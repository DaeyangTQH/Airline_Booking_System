package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.application.port.in.AirportUseCase;
import com.group3.flightsearch.application.query.AirportView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
public class AirportController {
    private final AirportUseCase useCase;

    @GetMapping
    public List<AirportView> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "20") int size) {
        return useCase.list(page, size);
    }

}
