package com.group3.infrastructure.adapter;

import com.group3.application.service.ReminderAppService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderSchedulerJob {

    private final ReminderAppService reminderAppService;

    public ReminderSchedulerJob(ReminderAppService reminderAppService) {
        this.reminderAppService = reminderAppService;
    }

    // Cứ mỗi 10 giây (10000 ms) sẽ quét DB 1 lần
    @Scheduled(fixedRate = 10000)
    public void runReminderJob() {
        reminderAppService.processDueReminders();
    }
}