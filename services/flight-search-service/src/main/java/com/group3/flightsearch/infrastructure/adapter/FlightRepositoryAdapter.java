package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.domain.aggregate.Flight;
import com.group3.flightsearch.domain.repository.FlightRepository;
import com.group3.flightsearch.domain.valueobject.FlightId;
import com.group3.flightsearch.infrastructure.persistence.mapper.FlightPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.FlightJpaRepository;
import com.group3.flightsearch.infrastructure.persistence.repository.FareJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FlightRepositoryAdapter implements FlightRepository {
    private final FlightJpaRepository flights;
    private final FareJpaRepository fares;
    private final FlightPersistenceMapper mapper;

    @Override
    public Optional<Flight> findByIdForUpdate(FlightId id) {
        var flight = flights.findByIdForUpdate(id.value());
        if (flight.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(flight.get(),
                fares.findAllByFlightIdOrderByFareClassAscValidFromAsc(id.value())));
    }
}
