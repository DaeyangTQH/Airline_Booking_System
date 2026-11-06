package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.BookingEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, UUID> {

    Optional<BookingEntity> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<BookingEntity> findAllByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM BookingEntity b WHERE b.id = :bookingId
    """)
    Optional<BookingEntity> findByIdForUpdate(
            @Param("bookingId") UUID bookingId
    );
    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
