package com.group3.api.rest;

import com.group3.api.dto.ReminderRequest;
import com.group3.application.service.ReminderAppService;
import com.group3.domain.aggregate.ScheduledReminder;
import com.group3.domain.vo.BookingRef;
import com.group3.domain.vo.Recipient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ReminderAppService service;

    public ReminderController(ReminderAppService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createReminder(@RequestBody ReminderRequest request) {
        try {
            ScheduledReminder reminder = ScheduledReminder.create(
                    new BookingRef(request.bookingRef()),
                    new Recipient(request.email()),
                    Instant.parse(request.departureTime()),
                    Instant.parse(request.remindAt())
            );
            service.scheduleReminder(reminder);
            return ResponseEntity.ok("Lên lịch nhắc nhở thành công! ID: " + reminder.getId().value());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi dữ liệu: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sai định dạng thời gian (Yêu cầu ISO-8601, VD: 2026-11-05T10:00:00Z)");
        }
    }
}