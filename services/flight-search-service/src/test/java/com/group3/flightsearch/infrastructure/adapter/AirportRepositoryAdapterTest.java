package com.group3.flightsearch.infrastructure.adapter;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;
import com.group3.flightsearch.infrastructure.persistence.mapper.AirportPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.AirportJpaRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AirportRepositoryAdapterTest {
    private final AirportJpaRepository jpa = mock(AirportJpaRepository.class);
    private final AirportRepositoryAdapter adapter =
            new AirportRepositoryAdapter(jpa, new AirportPersistenceMapper());
    private final Airport airport = Airport.register(new AirportId(UUID.randomUUID()),
            new AirportCode("HAN"), "Noi Bai", "Hanoi", "Vietnam");

    @Test
    void concurrentDuplicateUsesTheNamedUniqueConstraint() {
        var violation = new ConstraintViolationException(
                "duplicate", new SQLException("duplicate", "23505"), "uk_airports_code");
        when(jpa.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate", violation));
        var error = assertThrows(CustomException.class, () -> adapter.save(airport));
        assertEquals(ErrorType.CONFLICT, error.getType());
        assertEquals("AIRPORT_CODE_ALREADY_EXISTS", error.getCode());
    }

    @Test
    void unrelatedIntegrityErrorsAreNotMisreportedAsDuplicateCodes() {
        var failure = new DataIntegrityViolationException("other constraint");
        when(jpa.saveAndFlush(any())).thenThrow(failure);
        assertSame(failure, assertThrows(DataIntegrityViolationException.class, () -> adapter.save(airport)));
    }

    @Test
    void mapperRoundTripPreservesIdentityAndValues() {
        var mapper = new AirportPersistenceMapper();
        var restored = mapper.toDomain(mapper.toEntity(airport));
        assertEquals(airport.getId(), restored.getId());
        assertEquals(airport.getCode(), restored.getCode());
        assertEquals(airport.getName(), restored.getName());
        assertEquals(airport.getCity(), restored.getCity());
        assertEquals(airport.getCountry(), restored.getCountry());
    }
}
