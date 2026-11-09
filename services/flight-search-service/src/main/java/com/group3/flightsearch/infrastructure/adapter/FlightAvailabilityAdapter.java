package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.application.port.out.FlightAvailabilityPort;
import com.group3.flightsearch.application.query.FlightAvailabilityView;
import com.group3.flightsearch.infrastructure.persistence.entity.FlightAvailabilityJpaEntity;
import com.group3.flightsearch.infrastructure.persistence.repository.FlightAvailabilityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FlightAvailabilityAdapter implements FlightAvailabilityPort {
    private final FlightAvailabilityJpaRepository repository;

    @Override
    public List<FlightAvailabilityView> findByFlight(UUID flightId) {
        List<FlightAvailabilityView> result = new ArrayList<>();
        for (var row : repository.findAllByFlightIdOrderByFareClassAsc(flightId)) {
            result.add(view(row));
        }
        return List.copyOf(result);
    }

    @Override
    public List<FlightAvailabilityView> findByFlights(Collection<UUID> flightIds) {
        if (flightIds.isEmpty()) return List.of();
        List<FlightAvailabilityView> result = new ArrayList<>();
        for (var row : repository.findAllByFlightIdInOrderByFareClassAsc(flightIds)) {
            result.add(view(row));
        }
        return List.copyOf(result);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void refresh(UUID flightId) {
        repository.refresh(flightId);
    }

    private FlightAvailabilityView view(FlightAvailabilityJpaEntity entity) {
        return new FlightAvailabilityView(entity.getFlightId(), entity.getFareClass(),
                entity.getAvailableSeats(), entity.getClassCapacity());
    }
}
