package com.group3.api.dto;

public record NotificationMessage(
        String email,
        String type,
        String message
) {}