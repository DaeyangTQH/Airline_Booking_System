package com.group3.infrastructure.persistence.adapter;

import com.group3.application.port.out.PassengerRepositoryPort;
import com.group3.domain.aggregate.Passenger;
import com.group3.infrastructure.persistence.entity.PassengerJpaEntity;
import com.group3.infrastructure.persistence.mapper.PassengerPersistenceMapper;
import com.group3.infrastructure.persistence.repository.PassengerJpaRepository;
import com.group3.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PassengerPersistenceAdapter implements PassengerRepositoryPort {
    private final PassengerJpaRepository repo;
    private final UserJpaRepository userRepo;
    private final PassengerPersistenceMapper mapper;

    public PassengerPersistenceAdapter(PassengerJpaRepository repo, UserJpaRepository userRepo,
                                       PassengerPersistenceMapper mapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @Override
    public Passenger save(Passenger passenger) {
        PassengerJpaEntity entity = repo.findById(passenger.getId().value()).orElseGet(PassengerJpaEntity::new);
        mapper.copyToEntity(passenger, entity);
        entity.setUser(userRepo.getReferenceById(passenger.getUserId().value()));
        return mapper.toDomain(repo.saveAndFlush(entity));
    }

    @Override
    public Optional<Passenger> findOwned(UUID passengerId, UUID userId) {
        return repo.findByIdAndUser_Id(passengerId, userId).map(mapper::toDomain);
    }

    @Override
    public List<Passenger> findAllOwned(UUID userId) {
        return repo.findAllByUser_Id(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Passenger> findAllOwnedByIds(UUID userId, Collection<UUID> ids) {
        return repo.findAllByUser_IdAndIdIn(userId, ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(Passenger passenger) { repo.deleteById(passenger.getId().value()); }
}
