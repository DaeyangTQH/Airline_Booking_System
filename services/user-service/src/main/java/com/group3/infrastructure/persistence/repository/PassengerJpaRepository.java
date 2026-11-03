package com.group3.infrastructure.persistence.repository;

import com.group3.infrastructure.persistence.entity.PassengerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassengerJpaRepository extends JpaRepository<PassengerJpaEntity, UUID> {
    Optional<PassengerJpaEntity> findByIdAndUser_Id(UUID id, UUID userId);
    List<PassengerJpaEntity> findAllByUser_Id(UUID userId);
    List<PassengerJpaEntity> findAllByUser_IdAndIdIn(UUID userId, Collection<UUID> ids);
}
