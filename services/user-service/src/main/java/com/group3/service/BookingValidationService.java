
package com.group3.service;

import com.group3.dto.passenger.BookingValidationRequest;
import com.group3.dto.passenger.BookingValidationResponse;
import com.group3.dto.passenger.PassengerResponse;

import com.group3.entity.Passenger;
import com.group3.entity.User;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

import com.group3.repository.PassengerRepository;
import com.group3.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingValidationService {

    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;

    public BookingValidationService(
            UserRepository userRepository,
            PassengerRepository passengerRepository
    ) {
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
    }

    // ==========================================
    // VALIDATE PASSENGERS BEFORE BOOKING
    // ==========================================

    @Transactional(readOnly = true)
    public BookingValidationResponse validateForBooking(
            UUID userId,
            BookingValidationRequest request
    ) {

        // 1. Verify account exists
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                "USER_NOT_FOUND",
                                ErrorType.NOT_FOUND,
                                "User not found"
                        )
                );

        // 2. Verify account is active
        if (!user.isActive()) {
            throw new CustomException(
                    "ACCOUNT_DISABLED",
                    ErrorType.FORBIDDEN,
                    "This account has been disabled"
            );
        }

        // 3. Get requested passenger IDs
        List<UUID> passengerIds = request.getPassengerIds();

        if (passengerIds == null || passengerIds.isEmpty()) {
            throw new CustomException(
                    "EMPTY_PASSENGER_LIST",
                    ErrorType.VALIDATION,
                    "Passenger list cannot be empty"
            );
        }

        // 4. Reject null IDs
        if (passengerIds.stream().anyMatch(id -> id == null)) {
            throw new CustomException(
                    "INVALID_PASSENGER_ID",
                    ErrorType.VALIDATION,
                    "Passenger ID cannot be null"
            );
        }

        // 5. Reject duplicate IDs
        if (new HashSet<>(passengerIds).size()
                != passengerIds.size()) {

            throw new CustomException(
                    "DUPLICATE_PASSENGER",
                    ErrorType.VALIDATION,
                    "Passenger list contains duplicate IDs"
            );
        }

        // 6. Fetch only passengers owned by this user
        List<Passenger> foundPassengers =
                passengerRepository.findAllByUser_IdAndIdIn(
                        userId,
                        passengerIds
                );

        // 7. Every requested ID must belong to this user
        if (foundPassengers.size() != passengerIds.size()) {
            throw new CustomException(
                    "PASSENGER_NOT_FOUND",
                    ErrorType.NOT_FOUND,
                    "One or more passengers were not found"
            );
        }

        // 8. Map passengers by ID
        Map<UUID, Passenger> passengerMap = new HashMap<>();

        for (Passenger passenger : foundPassengers) {
            passengerMap.put(
                    passenger.getId(),
                    passenger
            );
        }

        // 9. Preserve the order supplied by Booking Service
        List<PassengerResponse> responses =
                passengerIds.stream()
                        .map(passengerMap::get)
                        .map(PassengerResponse::new)
                        .toList();

        // 10. Return validated passenger information
        return new BookingValidationResponse(
                user.getId(),
                responses
        );
    }
}
