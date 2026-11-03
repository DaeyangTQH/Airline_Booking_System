package com.group3.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BookingPassengerResponse(
        UUID id,
        UUID passengerProfileId,
        String fullName,
        String passportNumber,
        LocalDate dateOfBirth,
        String phone,
        String email
) {
}
