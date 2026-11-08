package com.group3.flightsearch.infrastructure.persistence.mapper;

import com.group3.flightsearch.domain.aggregate.Flight;
import com.group3.flightsearch.domain.model.Fare;
import com.group3.flightsearch.domain.valueobject.*;
import com.group3.flightsearch.infrastructure.persistence.entity.FlightJpaEntity;
import com.group3.flightsearch.infrastructure.persistence.entity.FareJpaEntity;
import org.springframework.stereotype.Component;
import java.util.Currency;
import java.util.List;

@Component
public class FlightPersistenceMapper {
    public FlightJpaEntity toEntity(Flight flight) {
        var entity = new FlightJpaEntity();
        entity.setId(flight.getId().value());
        entity.setFlightCode(flight.getCode().value());
        entity.setOriginCode(flight.getOriginCode().value());
        entity.setDestinationCode(flight.getDestinationCode().value());
        entity.setDepartureTime(flight.getDepartureTime());
        entity.setArrivalTime(flight.getArrivalTime());
        entity.setAircraftId(flight.getAircraftId().value());
        entity.setStatus(flight.getStatus());
        return entity;
    }

    public Flight toDomain(FlightJpaEntity flight, List<FareJpaEntity> fares) {
        return Flight.restore(new FlightId(flight.getId()), new FlightCode(flight.getFlightCode()),
                new AirportCode(flight.getOriginCode()), new AirportCode(flight.getDestinationCode()),
                flight.getDepartureTime(), flight.getArrivalTime(), new AircraftId(flight.getAircraftId()),
                flight.getStatus(), fares.stream().map(this::toDomain).toList());
    }

    public FareJpaEntity toEntity(Fare fare) {
        var entity = new FareJpaEntity();
        entity.setId(fare.getId().value());
        entity.setFlightId(fare.getFlightId().value());
        entity.setFareClass(fare.getFareClass());
        entity.setBasePrice(fare.getBasePrice().amount());
        entity.setCurrency(fare.getBasePrice().currency().getCurrencyCode());
        entity.setValidFrom(fare.getValidity().from());
        entity.setValidTo(fare.getValidity().to());
        return entity;
    }

    public Fare toDomain(FareJpaEntity fare) {
        return new Fare(new FareId(fare.getId()), new FlightId(fare.getFlightId()), fare.getFareClass(),
                new Money(fare.getBasePrice(), Currency.getInstance(fare.getCurrency())),
                new ValidityPeriod(fare.getValidFrom(), fare.getValidTo()));
    }
}
