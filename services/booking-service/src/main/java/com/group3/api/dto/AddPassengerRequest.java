package com.group3.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AddPassengerRequest(
        UUID passengerProfileId,

        @NotBlank(message = "Passenger full name must not be blank")
        @Size(
                max = 150,
                message = "Passenger full name must not exceed 150 characters"
        )
        String fullName,

        @NotBlank(message = "Passport number must not be blank")
        @Size(
                max = 50,
                message = "Passport number must not exceed 50 characters"
        )
        String passportNumber,

        @NotNull(message = "Date of birth is required")
        @PastOrPresent(message = "Date of birth must be in the past or present")
        LocalDate dateOfBirth,

        @NotNull(
                message = "Contact information is required"
        )
        @Valid
        BookingContactRequest contact
) {
}
