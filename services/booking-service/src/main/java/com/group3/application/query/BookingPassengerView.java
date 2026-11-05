package com.group3.application.query;


import java.time.LocalDate;
import java.util.UUID;

public record BookingPassengerView(
        UUID id,
        UUID passengerProfileId,
        String fullName,
        String passportNumber,
        LocalDate dateOfBirth,
        String phone,
        String email
) {
}

