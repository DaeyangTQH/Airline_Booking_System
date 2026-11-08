package com.group3.flightsearch.infrastructure.adapter;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.repository.AirportRepository;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;
import com.group3.flightsearch.infrastructure.persistence.mapper.AirportPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.AirportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AirportRepositoryAdapter implements AirportRepository {
    private final AirportJpaRepository repository;
    private final AirportPersistenceMapper mapper;

    @Override
    public Airport save(Airport airport) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(airport)));
        } catch (DataIntegrityViolationException exception) {
            // The pre-check alone cannot protect concurrent creates.
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_airports_code".equalsIgnoreCase(violation.getConstraintName())) {
                    throw new CustomException("AIRPORT_CODE_ALREADY_EXISTS", ErrorType.CONFLICT,
                            "Airport code already exists: " + airport.getCode().value());
                }
            }
            throw exception;
        }
    }

    @Override
    public Optional<Airport> findById(AirportId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(AirportCode code) {
        return repository.existsByCode(code.value());
    }

    @Override
    public List<Airport> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("code", "id")))
                .stream().map(mapper::toDomain).toList();
    }
}
