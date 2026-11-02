package com.group3.application.port.in;
import com.group3.domain.aggregate.Passenger;
import java.util.List;
import java.util.UUID;
public interface BookingValidationUseCase {
    List<Passenger> validate(UUID userId, List<UUID> passengerIds);
}
