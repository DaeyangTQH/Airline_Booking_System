
package com.group3.dto.passenger;

import java.util.List;
import java.util.UUID;

public class BookingValidationResponse {

    private UUID userId;

    private int passengerCount;

    private List<PassengerResponse> passengers;

    public BookingValidationResponse() {
    }

    public BookingValidationResponse(
            UUID userId,
            List<PassengerResponse> passengers
    ) {
        this.userId = userId;
        this.passengers = passengers;
        this.passengerCount = passengers.size();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public List<PassengerResponse> getPassengers() {
        return passengers;
    }

    public void setPassengers(
            List<PassengerResponse> passengers
    ) {
        this.passengers = passengers;
    }
}
