package com.group3.flightsearch.infrastructure.persistence.mapper;

import com.group3.flightsearch.domain.aggregate.Flight;
import com.group3.flightsearch.domain.model.Fare;
import com.group3.flightsearch.domain.valueobject.*;
import com.group3.flightsearch.infrastructure.persistence.entity.FlightJpaEntity;
import com.group3.flightsearch.infrastructure.persistence.entity.FareJpaEntity;
import org.springframework.stereotype.Component;
import java.util.Currency;
import java.util.List;
import java.util.ArrayList;

@Component
public class FlightPersistenceMapper {
    public Flight toDomain(FlightJpaEntity flight, List<FareJpaEntity> fares) {
        List<Fare> domainFares = new ArrayList<>();
        for (FareJpaEntity fare : fares) {
            domainFares.add(toDomain(fare));
        }
        return Flight.restore(new FlightId(flight.getId()), new FlightCode(flight.getFlightCode()),
                new AirportCode(flight.getOriginCode()), new AirportCode(flight.getDestinationCode()),
                flight.getDepartureTime(), flight.getArrivalTime(), new AircraftId(flight.getAircraftId()),
                flight.getStatus(), List.copyOf(domainFares));
    }

    public Fare toDomain(FareJpaEntity fare) {
        return new Fare(new FareId(fare.getId()), new FlightId(fare.getFlightId()), fare.getFareClass(),
                new Money(fare.getBasePrice(), Currency.getInstance(fare.getCurrency())),
                new ValidityPeriod(fare.getValidFrom(), fare.getValidTo()));
    }
}
