package com.group3.domain.vo;

import java.util.regex.Pattern;

public record Recipient(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    public Recipient {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Email không được để trống");
        if (!EMAIL_PATTERN.matcher(value).matches()) throw new IllegalArgumentException("Email sai định dạng");
    }
}