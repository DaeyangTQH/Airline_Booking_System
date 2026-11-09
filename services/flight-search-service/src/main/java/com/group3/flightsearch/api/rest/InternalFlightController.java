package com.group3.flightsearch.api.rest;

import com.group3.flightsearch.application.port.in.GetFareQuoteUseCase;
import com.group3.flightsearch.application.query.FareQuoteView;
import com.group3.flightsearch.domain.model.FareClass;
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

    @GetMapping("/{flightId}/fare-quote")
    public FareQuoteView getQuote(
            @PathVariable("flightId") UUID flightId,
            @RequestParam("fareClass") FareClass fareClass,
            @RequestParam("quantity") int quantity,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String apiKey) {
        internalApiKeyValidator.validate(apiKey);
        return useCase.getQuote(flightId, fareClass, quantity);
    }
}
