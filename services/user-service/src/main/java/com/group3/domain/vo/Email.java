package com.group3.domain.vo;

import com.group3.domain.exception.DomainValidationException;
import java.util.Locale;

public record Email(String value) {
    public Email {
        if (value == null) throw new DomainValidationException("Email is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 255 || !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new DomainValidationException("Invalid email format");
        }
    }
}
