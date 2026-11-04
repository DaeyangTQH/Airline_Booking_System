package com.group3.flightsearch.domain.aggregate;

import com.group3.flightsearch.domain.exception.InvalidDomainStateException;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.Fare;
import com.group3.flightsearch.domain.model.FlightStatus;
import com.group3.flightsearch.domain.valueobject.AircraftId;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.FareId;
import com.group3.flightsearch.domain.valueobject.FlightCode;
import com.group3.flightsearch.domain.valueobject.FlightId;
import com.group3.flightsearch.domain.valueobject.Money;
import com.group3.flightsearch.domain.valueobject.ValidityPeriod;
import com.group3.flightsearch.domain.model.FareClass;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Flight {
    private final FlightId id;
    private final FlightCode code;
    private final AirportCode originCode;
    private final AirportCode destinationCode;
    private final AircraftId aircraftId;
    private Instant departureTime;
    private Instant arrivalTime;
    private FlightStatus status;
    private final List<Fare> fares = new ArrayList<>();

    private Flight(FlightId id, FlightCode code, AirportCode originCode, AirportCode destinationCode,
                   Instant departureTime, Instant arrivalTime, AircraftId aircraftId, FlightStatus status) {
        if (id == null || code == null || originCode == null || destinationCode == null
                || aircraftId == null || status == null) {
            throw new InvalidDomainValueException("Flight fields are required");
        }
        validateRouteAndTime(originCode, destinationCode, departureTime, arrivalTime);
        this.id = id;
        this.code = code;
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.aircraftId = aircraftId;
        this.status = status;
    }

    public static Flight schedule(FlightId id, FlightCode code, AirportCode originCode,
                                  AirportCode destinationCode, Instant departureTime, Instant arrivalTime,
                                  AircraftId aircraftId) {
        return new Flight(id, code, originCode, destinationCode, departureTime, arrivalTime,
                aircraftId, FlightStatus.SCHEDULED);
    }

    public static Flight restore(FlightId id, FlightCode code, AirportCode originCode,
                                 AirportCode destinationCode, Instant departureTime, Instant arrivalTime,
                                 AircraftId aircraftId, FlightStatus status, List<Fare> fares) {
        if (fares == null) {
            throw new InvalidDomainValueException("Fares must not be null");
        }
        Flight flight = new Flight(id, code, originCode, destinationCode, departureTime, arrivalTime, aircraftId, status);
        fares.forEach(flight::addFare);
        return flight;
    }

    private static void validateRouteAndTime(AirportCode origin, AirportCode destination,
                                             Instant departure, Instant arrival) {
        if (origin.equals(destination)) {
            throw new InvalidDomainValueException("Origin and destination must differ");
        }
        if (departure == null || arrival == null || !arrival.isAfter(departure)) {
            throw new InvalidDomainValueException("Arrival must be after departure");
        }
    }

    public void reschedule(Instant departure, Instant arrival) {
        requireActive();
        validateRouteAndTime(originCode, destinationCode, departure, arrival);
        this.departureTime = departure;
        this.arrivalTime = arrival;
    }

    public void markDelayed() {
        if (status != FlightStatus.SCHEDULED) {
            throw new InvalidDomainStateException("Only scheduled flights can be delayed");
        }
        status = FlightStatus.DELAYED;
    }

    public void cancel() {
        requireActive();
        status = FlightStatus.CANCELLED;
    }

    public void complete() {
        requireActive();
        status = FlightStatus.COMPLETED;
    }

    private void requireActive() {
        if (status != FlightStatus.SCHEDULED && status != FlightStatus.DELAYED) {
            throw new InvalidDomainStateException("Flight is not active");
        }
    }

    public void addFare(Fare fare) {
        if (fare == null || !fare.getFlightId().equals(id)) {
            throw new InvalidDomainValueException("Fare must belong to this flight");
        }
        if (fares.stream().anyMatch(existing -> existing.getId().equals(fare.getId()))) {
            throw new InvalidDomainValueException("Fare id already exists");
        }
        ensureNoOverlap(fare, null);
        fares.add(fare);
    }

    public void changeFarePrice(FareId fareId, Money price) {
        int index = fareIndex(fareId);
        fares.set(index, fares.get(index).withPrice(price));
    }

    public void changeFareValidity(FareId fareId, ValidityPeriod validity) {
        int index = fareIndex(fareId);
        Fare changed = fares.get(index).withValidity(validity);
        ensureNoOverlap(changed, fareId);
        fares.set(index, changed);
    }

    private int fareIndex(FareId fareId) {
        if (fareId == null) {
            throw new InvalidDomainValueException("Fare id is required");
        }
        for (int i = 0; i < fares.size(); i++) {
            if (fares.get(i).getId().equals(fareId)) {
                return i;
            }
        }
        throw new InvalidDomainValueException("Fare not found");
    }

    // Nonoverlapping intervals per class make the current fare unambiguous.
    private void ensureNoOverlap(Fare candidate, FareId excludedId) {
        boolean overlaps = fares.stream().anyMatch(existing ->
                (excludedId == null || !existing.getId().equals(excludedId))
                && existing.getFareClass() == candidate.getFareClass()
                && existing.getValidity().overlaps(candidate.getValidity()));
        if (overlaps) {
            throw new InvalidDomainValueException("Fare validity overlaps another fare in the same class");
        }
    }

    public FlightId getId() { return id; }
    public FlightCode getCode() { return code; }
    public AirportCode getOriginCode() { return originCode; }
    public AirportCode getDestinationCode() { return destinationCode; }
    public Instant getDepartureTime() { return departureTime; }
    public Instant getArrivalTime() { return arrivalTime; }
    public AircraftId getAircraftId() { return aircraftId; }
    public FlightStatus getStatus() { return status; }
    public List<Fare> getFares() { return List.copyOf(fares); }
}
