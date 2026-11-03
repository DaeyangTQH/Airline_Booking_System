package com.group3.domain.repository;

import com.group3.domain.aggregate.ScheduledReminder;
import java.time.Instant;
import java.util.List;

public interface ScheduledReminderRepository {
    ScheduledReminder save(ScheduledReminder reminder);
    // Hàm quan trọng để Scheduler gọi
    List<ScheduledReminder> findDueReminders(Instant currentTime);
}