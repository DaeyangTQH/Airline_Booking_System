package com.group3.infrastructure.persistence;

import com.group3.domain.aggregate.ScheduledReminder;
import com.group3.domain.repository.ScheduledReminderRepository;
import com.group3.domain.vo.BookingRef;
import com.group3.domain.vo.Recipient;
import com.group3.domain.vo.ReminderId;
import com.group3.infrastructure.persistence.entity.ScheduledReminderJpaEntity;
import com.group3.infrastructure.persistence.jpa.JpaScheduledReminderRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ScheduledReminderRepositoryImpl implements ScheduledReminderRepository {

    private final JpaScheduledReminderRepository jpaRepository;

    public ScheduledReminderRepositoryImpl(JpaScheduledReminderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ScheduledReminder save(ScheduledReminder reminder) {
        ScheduledReminderJpaEntity saved = jpaRepository.save(toEntity(reminder));
        return toDomain(saved);
    }

    @Override
    public List<ScheduledReminder> findDueReminders(Instant currentTime) {
        return jpaRepository.findDueReminders(currentTime).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // Mapper
    private ScheduledReminderJpaEntity toEntity(ScheduledReminder d) {
        ScheduledReminderJpaEntity e = new ScheduledReminderJpaEntity();
        e.setId(d.getId().value());
        e.setBookingRef(d.getBookingRef().value());
        e.setContactEmail(d.getContactEmail().value());
        e.setDepartureTime(d.getDepartureTime());
        e.setRemindAt(d.getRemindAt());
        e.setSent(d.isSent());
        return e;
    }

    private ScheduledReminder toDomain(ScheduledReminderJpaEntity e) {
        return new ScheduledReminder(
                new ReminderId(e.getId()),
                new BookingRef(e.getBookingRef()),
                new Recipient(e.getContactEmail()),
                e.getDepartureTime(),
                e.getRemindAt(),
                e.isSent()
        );
    }
}