package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.command.*;
import com.group3.flightsearch.application.port.in.FlightUseCase;
import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.port.out.FlightAvailabilityPort;
import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.aggregate.Flight;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.Fare;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.repository.AirportRepository;
import com.group3.flightsearch.domain.repository.AircraftRepository;
import com.group3.flightsearch.domain.repository.FlightRepository;
import com.group3.flightsearch.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService implements FlightUseCase {
    private final FlightRepository repository;
    private final AirportRepository airports;
    private final AircraftRepository aircrafts;
    private final FlightQueryPort queryPort;
    private final FlightAvailabilityPort availability;

    @Override
    @Transactional
    public FlightView create(CreateFlightCommand command) {
        if (command == null) throw new InvalidDomainValueException("Create flight command is required");
        AirportCode origin = new AirportCode(command.originCode());
        AirportCode destination = new AirportCode(command.destinationCode());
        AircraftId aircraftId = new AircraftId(command.aircraftId());
        if (!airports.existsByCode(origin) || !airports.existsByCode(destination)) {
            throw missing("AIRPORT_NOT_FOUND", "Origin or destination airport does not exist");
        }
        if (aircrafts.findById(aircraftId).isEmpty()) {
            throw missing("AIRCRAFT_NOT_FOUND", "Aircraft does not exist: " + command.aircraftId());
        }
        Flight flight = Flight.schedule(new FlightId(UUID.randomUUID()), new FlightCode(command.flightCode()),
                origin, destination, command.departureTime(), command.arrivalTime(), aircraftId);
        repository.save(flight);
        availability.refresh(flight.getId().value());
        return getById(flight.getId().value());
    }

    @Override
    @Transactional
    public FlightView updateSchedule(UpdateFlightScheduleCommand command) {
        if (command == null) throw new InvalidDomainValueException("Update schedule command is required");
        Flight flight = loadForUpdate(command.flightId());
        flight.reschedule(command.departureTime(), command.arrivalTime());
        repository.save(flight);
        return getById(command.flightId());
    }

    @Override
    public FlightView getById(UUID id) {
        new FlightId(id);
        return queryPort.findById(id).orElseThrow(() -> missing("FLIGHT_NOT_FOUND", "Flight not found: " + id));
    }

    @Override
    public List<FlightView> search(String origin, String destination, LocalDate date,
                                    FareClass fareClass, int quantity, int page, int size) {
        if (quantity < 1 || quantity > 9) {
            throw new InvalidDomainValueException("Quantity must be between 1 and 9");
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        String originCode = origin == null ? null : new AirportCode(origin).value();
        String destinationCode = destination == null ? null : new AirportCode(destination).value();
        if (originCode != null && originCode.equals(destinationCode)) {
            throw new InvalidDomainValueException("Origin and destination must differ");
        }
        return queryPort.search(new FlightSearchQuery(originCode, destinationCode, date,
                fareClass, Instant.now(), quantity, page, size));
    }

    @Override
    @Transactional
    public FareView addFare(CreateFareCommand command) {
        if (command == null) throw new InvalidDomainValueException("Create fare command is required");
        Flight flight = loadForUpdate(command.flightId());
        Fare fare = new Fare(new FareId(UUID.randomUUID()), flight.getId(), command.fareClass(),
                money(command.basePrice(), command.currency()), new ValidityPeriod(command.validFrom(), command.validTo()));
        flight.addFare(fare);
        repository.save(flight);
        return FareView.from(fare);
    }

    @Override
    @Transactional
    public FareView updateFare(UpdateFareCommand command) {
        if (command == null) throw new InvalidDomainValueException("Update fare command is required");
        Flight flight = loadForUpdate(command.flightId());
        FareId fareId = new FareId(command.fareId());
        if (flight.getFares().stream().noneMatch(fare -> fare.getId().equals(fareId))) {
            throw missing("FARE_NOT_FOUND", "Fare does not belong to this flight: " + command.fareId());
        }
        flight.changeFarePrice(fareId, money(command.basePrice(), command.currency()));
        flight.changeFareValidity(fareId, new ValidityPeriod(command.validFrom(), command.validTo()));
        repository.save(flight);
        return flight.getFares().stream().filter(fare -> fare.getId().equals(fareId))
                .findFirst().map(FareView::from).orElseThrow();
    }

    @Override
    public List<FareView> getFares(UUID flightId) {
        return getById(flightId).fares();
    }

    @Override
    public FareView getFare(UUID flightId, UUID fareId) {
        new FareId(fareId);
        return getFares(flightId).stream().filter(fare -> fare.id().equals(fareId)).findFirst()
                .orElseThrow(() -> missing("FARE_NOT_FOUND", "Fare does not belong to this flight: " + fareId));
    }

    private Flight loadForUpdate(UUID id) {
        return repository.findByIdForUpdate(new FlightId(id))
                .orElseThrow(() -> missing("FLIGHT_NOT_FOUND", "Flight not found: " + id));
    }

    private Money money(BigDecimal amount, String currency) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new InvalidDomainValueException("Currency must be a three-letter uppercase currency code");
        }
        try {
            return new Money(amount, Currency.getInstance(currency));
        } catch (IllegalArgumentException exception) {
            throw new InvalidDomainValueException("Invalid amount or currency");
        }
    }

    private CustomException missing(String code, String message) {
        return new CustomException(code, ErrorType.NOT_FOUND, message);
    }
}
