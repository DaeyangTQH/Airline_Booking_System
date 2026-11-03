package com.group3.infrastructure.persistence.jpa;

import com.group3.infrastructure.persistence.entity.ScheduledReminderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaScheduledReminderRepository extends JpaRepository<ScheduledReminderJpaEntity, UUID> {
    // Query tìm các reminder đã đến giờ và chưa gửi
    @Query("SELECT r FROM ScheduledReminderJpaEntity r WHERE r.sent = false AND r.remindAt <= :currentTime")
    List<ScheduledReminderJpaEntity> findDueReminders(@Param("currentTime") Instant currentTime);
}