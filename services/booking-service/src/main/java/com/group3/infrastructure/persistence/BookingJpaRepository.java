package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, UUID> {

    Optional<BookingEntity> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<BookingEntity> findAllByUserId(UUID userId);
}
