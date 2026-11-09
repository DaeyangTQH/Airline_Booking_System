package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingPassengerJpaRepository extends JpaRepository<BookingPassengerEntity, UUID> {

    List<BookingPassengerEntity> findAllByBooking_Id(UUID bookingId);

    @Query("""
        SELECT p.booking.id
        FROM BookingPassengerEntity p
        WHERE p.id = :passengerId
""")
    Optional<UUID> findBookingIdByPassengerId(
            @Param("passengerId")
            UUID passengerId
    );

}
