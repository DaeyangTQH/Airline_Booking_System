
package com.group3.application.service;

import com.group3.application.port.in.BookingValidationUseCase;
import com.group3.application.port.out.PassengerRepositoryPort;
import com.group3.application.port.out.UserRepositoryPort;

import com.group3.domain.aggregate.Passenger;
import com.group3.domain.aggregate.User;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingValidationService
        implements BookingValidationUseCase {

    private final UserRepositoryPort userRepository;
    private final PassengerRepositoryPort passengerRepository;

    public BookingValidationService(
            UserRepositoryPort userRepository,
            PassengerRepositoryPort passengerRepository
    ) {
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
    }

    // ==========================================
    // VALIDATE PASSENGERS BEFORE BOOKING
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Passenger> validate(
            UUID userId,
            List<UUID> passengerIds
    ) {

        // 1. Verify current user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                "USER_NOT_FOUND",
                                ErrorType.NOT_FOUND,
                                "User not found"
                        )
                );

        // 2. Check account status
        if (!user.isActive()) {
            throw new CustomException(
                    "ACCOUNT_DISABLED",
                    ErrorType.FORBIDDEN,
                    "This account has been disabled"
            );
        }

        // 3. Validate passenger list
        if (passengerIds == null || passengerIds.isEmpty()) {
            throw new CustomException(
                    "EMPTY_PASSENGER_LIST",
                    ErrorType.VALIDATION,
                    "Passenger list cannot be empty"
            );
        }

        // 4. Reject null passenger IDs
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

        // 6. Fetch passengers owned by current user
        List<Passenger> passengers =
                passengerRepository.findAllOwnedByIds(
                        userId,
                        passengerIds
                );

        // 7. Verify all passengers exist
        // and belong to current user
        if (passengers.size() != passengerIds.size()) {
            throw new CustomException(
                    "PASSENGER_NOT_FOUND",
                    ErrorType.NOT_FOUND,
                    "One or more passengers were not found"
            );
        }

        // 8. Create map for quick lookup
        Map<UUID, Passenger> passengerMap = new HashMap<>();

        for (Passenger passenger : passengers) {
            passengerMap.put(
                    passenger.getId().value(),
                    passenger
            );
        }

        // 9. Preserve original passenger order
        return passengerIds.stream()
                .map(passengerMap::get)
                .toList();
    }
}
