package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingPassengerJpaRepository extends JpaRepository<BookingPassengerEntity, UUID> {

    List<BookingPassengerEntity> findAllByBooking_Id(UUID bookingId);

    Long countByBooking_Id(UUID bookingId);

    boolean existsByIdAndBooking_Id(UUID passengerId, UUID bookingId);

    void deleteAllByBooking_Id(UUID bookingId);
}
