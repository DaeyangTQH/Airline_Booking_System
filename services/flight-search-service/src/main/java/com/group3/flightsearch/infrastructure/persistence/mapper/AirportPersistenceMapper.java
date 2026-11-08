package com.group3.flightsearch.infrastructure.persistence.mapper;

import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;
import com.group3.flightsearch.infrastructure.persistence.entity.AirportJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AirportPersistenceMapper {
    public AirportJpaEntity toEntity(Airport airport) {
        AirportJpaEntity entity = new AirportJpaEntity();
        entity.setId(airport.getId().value());
        entity.setCode(airport.getCode().value());
        entity.setName(airport.getName());
        entity.setCity(airport.getCity());
        entity.setCountry(airport.getCountry());
        return entity;
    }

    public Airport toDomain(AirportJpaEntity entity) {
        return Airport.register(new AirportId(entity.getId()), new AirportCode(entity.getCode()),
                entity.getName(), entity.getCity(), entity.getCountry());
    }
}
