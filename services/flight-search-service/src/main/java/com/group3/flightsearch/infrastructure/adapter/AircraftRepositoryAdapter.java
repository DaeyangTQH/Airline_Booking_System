package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.domain.aggregate.Aircraft;
import com.group3.flightsearch.domain.repository.AircraftRepository;
import com.group3.flightsearch.domain.valueobject.AircraftId;
import com.group3.flightsearch.infrastructure.persistence.mapper.AircraftPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.AircraftJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AircraftRepositoryAdapter implements AircraftRepository {
    private final AircraftJpaRepository repository;
    private final AircraftPersistenceMapper mapper;

    @Override
    public Aircraft save(Aircraft aircraft) {
        return mapper.toDomain(repository.save(mapper.toEntity(aircraft)));
    }

    @Override
    public Optional<Aircraft> findById(AircraftId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Aircraft> findByIdForUpdate(AircraftId id) {
        return repository.findByIdForUpdate(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Aircraft> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("model", "id")))
                .stream().map(mapper::toDomain).toList();
    }
}
