package com.group3.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record AddPassengerCommand(
        UUID bookingId,
        UUID userId,
        UUID passengerProfileId,
        String fullName,
        String passportNumber,
        LocalDate dateOfBirth,
        String phone,
        String email
) {
}
