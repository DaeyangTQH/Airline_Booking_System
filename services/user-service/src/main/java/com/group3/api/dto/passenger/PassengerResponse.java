package com.group3.api.dto.passenger;
import com.group3.domain.aggregate.Passenger;
import java.time.LocalDate;
import java.util.UUID;
public record PassengerResponse(UUID id, UUID userId, String fullName, String passportNo,
                                LocalDate dob, String phone, String email) {
    public static PassengerResponse from(Passenger p) {
        return new PassengerResponse(p.getId().value(), p.getUserId().value(), p.getFullName(),
                p.getPassportNo(), p.getDob(), p.getPhone(), p.getEmail());
    }
}
