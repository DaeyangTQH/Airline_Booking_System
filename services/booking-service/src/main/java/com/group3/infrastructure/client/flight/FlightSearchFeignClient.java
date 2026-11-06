package com.group3.infrastructure.client.flight;

import com.group3.infrastructure.client.flight.dto.FlightFareResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "flight-search-service",
        path = "/internal/v1/flights"
)
public interface FlightSearchFeignClient {

    @GetMapping("/{flightId}/fare-quote")
    FlightFareResponse getFareQuote(
            @PathVariable("flightId") UUID flightId,

            @RequestParam("fareClass") String fareClass,

            @RequestParam("quantity") int quantity
    );
}
