package com.group3.flightsearch.infrastructure.persistence.mapper;

import com.group3.flightsearch.domain.aggregate.Aircraft;
import com.group3.flightsearch.domain.valueobject.AircraftId;
import com.group3.flightsearch.domain.valueobject.AircraftModel;
import com.group3.flightsearch.domain.valueobject.SeatCapacity;
import com.group3.flightsearch.infrastructure.persistence.entity.AircraftJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AircraftPersistenceMapper {
    public AircraftJpaEntity toEntity(Aircraft aircraft) {
        AircraftJpaEntity entity = new AircraftJpaEntity();
        entity.setId(aircraft.getId().value());
        entity.setModel(aircraft.getModel().value());
        entity.setTotalSeats(aircraft.getTotalSeats().value());
        return entity;
    }

    public Aircraft toDomain(AircraftJpaEntity entity) {
        return Aircraft.register(new AircraftId(entity.getId()), new AircraftModel(entity.getModel()),
                new SeatCapacity(entity.getTotalSeats()));
    }
}
