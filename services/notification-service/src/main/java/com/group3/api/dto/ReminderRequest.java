package com.group3.api.dto;

public record ReminderRequest(
        String bookingRef,
        String email,
        String departureTime, // Định dạng ISO-8601: "2026-11-10T10:00:00Z"
        String remindAt       // Định dạng ISO-8601
) {}