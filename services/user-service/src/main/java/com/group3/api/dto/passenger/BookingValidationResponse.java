package com.group3.api.dto.passenger;
import java.util.List;
import java.util.UUID;
public record BookingValidationResponse(UUID userId, int passengerCount,
                                        List<PassengerResponse> passengers) { }
