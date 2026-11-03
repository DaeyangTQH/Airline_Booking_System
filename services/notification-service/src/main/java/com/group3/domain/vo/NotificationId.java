package com.group3.domain.vo;

import java.util.UUID;

public record NotificationId(UUID value) {
    public NotificationId() {
        this(UUID.randomUUID());
    }
}