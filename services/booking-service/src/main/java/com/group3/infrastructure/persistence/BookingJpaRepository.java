package com.group3.infrastructure.persistence;

import com.group3.domain.aggregate.BookingStatus;
import com.group3.infrastructure.persistence.entity.BookingEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, UUID> {

    Optional<BookingEntity> findByIdAndUserId(
            UUID bookingId,
            UUID userId
    );

    Optional<BookingEntity> findByBookingReferenceAndUserId(
            String bookingReference,
            UUID userId
    );

    boolean existsByBookingReference(String bookingReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM BookingEntity b WHERE b.id = :bookingId
    """)
    Optional<BookingEntity> findByIdForUpdate(
            @Param("bookingId") UUID bookingId
    );
    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE BookingEntity b
        SET b.status = :cancelledStatus, b.updatedAt =:now
        WHERE b.status = :pendingStatus AND b.expiresAt <= :now
""")
    int expirePendingBookings(
            @Param("now") Instant now,
            @Param("pendingStatus") BookingStatus pendingStatus,
            @Param("cancelledStatus") BookingStatus cancelledStatus
            );
}
