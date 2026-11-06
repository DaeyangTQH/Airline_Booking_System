package com.group3.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BookingContactRequest(
        @NotBlank(message = "Phone must not be blank")
        @Pattern(
                regexp = "^0\\d{9}$",
                message = "Invalid phone number"
        )
        String phone,

        @NotBlank(message = "Email must not be blank")
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$",
                message = "Invalid email address"
        )
        String email
) {
}
