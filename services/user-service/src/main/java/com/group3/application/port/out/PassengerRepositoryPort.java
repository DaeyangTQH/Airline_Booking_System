package com.group3.application.port.out;

import com.group3.domain.aggregate.Passenger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassengerRepositoryPort {
    Passenger save(Passenger passenger);
    Optional<Passenger> findOwned(UUID passengerId, UUID userId);
    List<Passenger> findAllOwned(UUID userId);
    List<Passenger> findAllOwnedByIds(UUID userId, Collection<UUID> ids);
    void delete(Passenger passenger);
}
