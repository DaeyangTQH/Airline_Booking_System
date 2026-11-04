package com.group3.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scheduled_reminders")
public class ScheduledReminderJpaEntity {
    @Id
    private UUID id;
    private String bookingRef;
    private String contactEmail;
    private Instant departureTime;
    private Instant remindAt;
    private boolean sent;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Instant getDepartureTime() { return departureTime; }
    public void setDepartureTime(Instant departureTime) { this.departureTime = departureTime; }
    public Instant getRemindAt() { return remindAt; }
    public void setRemindAt(Instant remindAt) { this.remindAt = remindAt; }
    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
}