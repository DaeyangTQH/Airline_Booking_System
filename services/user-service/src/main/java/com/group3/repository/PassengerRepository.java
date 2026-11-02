
package com.group3.repository;

import com.group3.entity.Passenger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PassengerRepository
        extends JpaRepository<Passenger, UUID> {

    // Find all passengers belonging to a user
    List<Passenger> findAllByUser_Id(UUID userId);

    // Find a specific passenger belonging to a user
    Optional<Passenger> findByIdAndUser_Id(
            UUID passengerId,
            UUID userId
    );

    // Check ownership
    boolean existsByIdAndUser_Id(
            UUID passengerId,
            UUID userId
    );

    // Validate multiple passengers for a booking
    List<Passenger> findAllByUser_IdAndIdIn(
            UUID userId,
            Collection<UUID> passengerIds
    );
}
