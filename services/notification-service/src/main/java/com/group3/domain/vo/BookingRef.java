package com.group3.domain.vo;

public record BookingRef(String value) {
    public BookingRef {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Mã đặt chỗ không được để trống");
        }
    }
}