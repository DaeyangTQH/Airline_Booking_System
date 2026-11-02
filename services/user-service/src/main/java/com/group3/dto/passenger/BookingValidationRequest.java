
package com.group3.dto.passenger;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class BookingValidationRequest {

    @NotEmpty(message = "Passenger list cannot be empty")
    private List<@NotNull(message = "Passenger ID cannot be null") UUID>
            passengerIds;

    public BookingValidationRequest() {
    }

    public List<UUID> getPassengerIds() {
        return passengerIds;
    }

    public void setPassengerIds(List<UUID> passengerIds) {
        this.passengerIds = passengerIds;
    }
}
