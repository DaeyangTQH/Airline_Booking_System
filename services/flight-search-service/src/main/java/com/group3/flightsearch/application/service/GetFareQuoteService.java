package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.in.GetFareQuoteUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.FareQuoteView;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.model.FlightStatus;
import com.group3.flightsearch.domain.valueobject.FlightId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFareQuoteService implements GetFareQuoteUseCase {
    // Booking's CurrencyCode currently supports these currencies only.
    private static final Set<String> BOOKING_CURRENCIES = Set.of("VND", "USD");
    private final FlightQueryPort flights;

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public FareQuoteView getQuote(UUID flightId, FareClass fareClass, int quantity) {
        new FlightId(flightId);
        if (fareClass == null) throw new InvalidDomainValueException("Fare class is required");
        // Booking TicketQuantity accepts any positive integer, unlike the public 1-9 search UI.
        if (quantity <= 0) throw new InvalidDomainValueException("Quantity must be greater than zero");
        Instant pricedAt = Instant.now();
        var flight = flights.findById(flightId).orElseThrow(() -> new CustomException(
                "FLIGHT_NOT_FOUND", ErrorType.NOT_FOUND, "Flight not found: " + flightId));
        var currentFares = flight.fares().stream()
                .filter(fare -> fare.fareClass() == fareClass)
                .filter(fare -> !pricedAt.isBefore(fare.validFrom()) && pricedAt.isBefore(fare.validTo()))
                .toList();
        if (currentFares.isEmpty()) {
            throw new CustomException("FARE_NOT_FOUND", ErrorType.NOT_FOUND,
                    "No current fare for flight " + flightId + " and class " + fareClass);
        }
        if (currentFares.size() != 1) {
            throw new CustomException("AMBIGUOUS_CURRENT_FARE", ErrorType.INTERNAL,
                    "More than one current fare exists for this flight and class");
        }
        var fare = currentFares.getFirst();
        if (!BOOKING_CURRENCIES.contains(fare.currency())) {
            throw new CustomException("FARE_CURRENCY_UNSUPPORTED", ErrorType.CONFLICT,
                    "Booking Service currently supports VND and USD fares only");
        }
        int availableSeats = flight.availability().stream().filter(row -> row.fareClass() == fareClass)
                .findFirst().orElseThrow(() -> new CustomException("FLIGHT_AVAILABILITY_UNAVAILABLE",
                        ErrorType.INTERNAL, "Availability projection is missing for this flight and class"))
                .availableSeats();
        // Booking checks availableSeats against quantity separately after checking bookable.
        boolean bookable = (flight.status() == FlightStatus.SCHEDULED || flight.status() == FlightStatus.DELAYED)
                && flight.departureTime().isAfter(pricedAt);
        return new FareQuoteView(flightId, fareClass, fare.basePrice(), fare.currency(), availableSeats, bookable);
    }
}
