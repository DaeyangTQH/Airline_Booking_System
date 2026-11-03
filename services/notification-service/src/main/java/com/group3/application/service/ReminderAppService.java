package com.group3.application.service;

import com.group3.domain.aggregate.Notification;
import com.group3.domain.aggregate.ScheduledReminder;
import com.group3.domain.repository.ScheduledReminderRepository;
import com.group3.domain.vo.NotificationType;
import com.group3.domain.vo.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReminderAppService {

    private final ScheduledReminderRepository reminderRepository;
    private final NotificationAppService notificationAppService;

    public ReminderAppService(ScheduledReminderRepository reminderRepository,
                              NotificationAppService notificationAppService) {
        this.reminderRepository = reminderRepository;
        this.notificationAppService = notificationAppService;
    }

    public ScheduledReminder scheduleReminder(ScheduledReminder reminder) {
        return reminderRepository.save(reminder);
    }

    // Hàm xử lý chính được Scheduler gọi
    public void processDueReminders() {
        List<ScheduledReminder> dueReminders = reminderRepository.findDueReminders(Instant.now());

        for (ScheduledReminder reminder : dueReminders) {
            // 1. Tạo thông báo (tái sử dụng NotificationAppService)
            String message = "Chuyến bay của bạn (Mã: " + reminder.getBookingRef().value() + ") sẽ khởi hành lúc " + reminder.getDepartureTime();
            Notification noti = Notification.create(
                    reminder.getContactEmail(),
                    NotificationType.FLIGHT_REMINDER,
                    new Payload(message)
            );

            // 2. Gửi thông báo
            notificationAppService.handleNotification(noti);

            // 3. Đánh dấu reminder đã gửi
            reminder.markAsSent();
            reminderRepository.save(reminder);

            System.out.println("[Scheduler] Đã gửi nhắc nhở cho booking: " + reminder.getBookingRef().value());
        }
    }
}