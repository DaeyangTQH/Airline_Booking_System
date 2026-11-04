package com.group3.domain.aggregate;

import com.group3.domain.vo.*;
import java.time.Instant;

public class ScheduledReminder {
    private final ReminderId id;
    private final BookingRef bookingRef;
    private final Recipient contactEmail;
    private final Instant departureTime;
    private final Instant remindAt;
    private boolean sent;

    public ScheduledReminder(ReminderId id, BookingRef bookingRef, Recipient contactEmail,
                             Instant departureTime, Instant remindAt, boolean sent) {
        this.id = id;
        this.bookingRef = bookingRef;
        this.contactEmail = contactEmail;
        this.departureTime = departureTime;
        this.remindAt = remindAt;
        this.sent = sent;
    }

    public static ScheduledReminder create(BookingRef bookingRef, Recipient contactEmail,
                                           Instant departureTime, Instant remindAt) {
        if (remindAt.isAfter(departureTime)) {
            throw new IllegalArgumentException("Thời gian nhắc nhở không được sau giờ khởi hành!");
        }
        return new ScheduledReminder(new ReminderId(), bookingRef, contactEmail, departureTime, remindAt, false);
    }

    public void markAsSent() {
        if (this.sent) {
            throw new IllegalStateException("Lịch nhắc nhở này đã được gửi rồi!");
        }
        this.sent = true;
    }

    // Getters
    public ReminderId getId() { return id; }
    public BookingRef getBookingRef() { return bookingRef; }
    public Recipient getContactEmail() { return contactEmail; }
    public Instant getDepartureTime() { return departureTime; }
    public Instant getRemindAt() { return remindAt; }
    public boolean isSent() { return sent; }
}