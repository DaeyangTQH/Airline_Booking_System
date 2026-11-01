package com.group3.flightsearch.domain.model;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.valueobject.FareId;
import com.group3.flightsearch.domain.valueobject.FlightId;
import com.group3.flightsearch.domain.valueobject.Money;
import com.group3.flightsearch.domain.valueobject.ValidityPeriod;

/** Immutable child entity. A Flight replaces it when its price or validity changes. */
public final class Fare {
    private final FareId id;
    private final FlightId flightId;
    private final FareClass fareClass;
    private final Money basePrice;
    private final ValidityPeriod validity;

    public Fare(FareId id, FlightId flightId, FareClass fareClass, Money basePrice, ValidityPeriod validity) {
        if (id == null || flightId == null || fareClass == null || basePrice == null || validity == null) {
            throw new InvalidDomainValueException("All fare fields are required");
        }
        this.id = id;
        this.flightId = flightId;
        this.fareClass = fareClass;
        this.basePrice = basePrice;
        this.validity = validity;
    }

    public Fare withPrice(Money price) {
        return new Fare(id, flightId, fareClass, price, validity);
    }

    public Fare withValidity(ValidityPeriod period) {
        return new Fare(id, flightId, fareClass, basePrice, period);
    }

    public FareId getId() { return id; }
    public FlightId getFlightId() { return flightId; }
    public FareClass getFareClass() { return fareClass; }
    public Money getBasePrice() { return basePrice; }
    public ValidityPeriod getValidity() { return validity; }
}
