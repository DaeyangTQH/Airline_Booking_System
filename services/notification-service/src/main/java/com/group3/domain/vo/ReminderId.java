package com.group3.domain.vo;

import java.util.UUID;

public record ReminderId(UUID value) {
    public ReminderId() {
        this(UUID.randomUUID());
    }
}