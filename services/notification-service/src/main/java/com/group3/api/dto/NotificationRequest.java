package com.group3.api.dto;

public record NotificationRequest(
        String email,
        String type,
        String message
) {}