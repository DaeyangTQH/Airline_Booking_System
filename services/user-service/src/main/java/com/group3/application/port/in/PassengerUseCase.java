package com.group3.application.port.in;
import com.group3.application.command.PassengerCommand;
import com.group3.domain.aggregate.Passenger;
import java.util.List;
import java.util.UUID;
public interface PassengerUseCase {
    Passenger create(UUID userId, PassengerCommand command);
    List<Passenger> list(UUID userId);
    Passenger get(UUID userId, UUID passengerId);
    Passenger update(UUID userId, UUID passengerId, PassengerCommand command);
    void delete(UUID userId, UUID passengerId);
}
