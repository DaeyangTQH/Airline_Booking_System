package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.domain.aggregate.Flight;
import com.group3.flightsearch.domain.repository.FlightRepository;
import com.group3.flightsearch.domain.valueobject.FlightId;
import com.group3.flightsearch.infrastructure.persistence.mapper.FlightPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.FlightJpaRepository;
import com.group3.flightsearch.infrastructure.persistence.repository.FareJpaRepository;
import com.group3.flightsearch.infrastructure.persistence.repository.AirportJpaRepository;
import com.group3.flightsearch.infrastructure.persistence.repository.AircraftJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FlightRepositoryAdapter implements FlightRepository {
    private final FlightJpaRepository flights;
    private final FareJpaRepository fares;
    private final FlightPersistenceMapper mapper;
    private final AirportJpaRepository airports;
    private final AircraftJpaRepository aircrafts;

    @Override
    public Flight save(Flight flight) {
        var entity = mapper.toEntity(flight);
        entity.setOrigin(airports.findByCode(flight.getOriginCode().value()).orElseThrow());
        entity.setDestination(airports.findByCode(flight.getDestinationCode().value()).orElseThrow());
        entity.setAircraft(aircrafts.getReferenceById(flight.getAircraftId().value()));
        var saved = flights.saveAndFlush(entity);
        fares.saveAllAndFlush(flight.getFares().stream().map(fare -> {
            var fareEntity = mapper.toEntity(fare);
            fareEntity.setFlight(saved);
            return fareEntity;
        }).toList());
        return flight;
    }

    @Override
    public Optional<Flight> findByIdForUpdate(FlightId id) {
        return flights.findByIdForUpdate(id.value())
                .map(flight -> mapper.toDomain(flight, fares.findAllByFlightIdOrderByFareClassAscValidFromAsc(id.value())));
    }
}
