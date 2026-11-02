
package com.group3.service;

import com.group3.dto.passenger.PassengerRequest;
import com.group3.dto.passenger.PassengerResponse;
import com.group3.entity.Passenger;
import com.group3.entity.User;
import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.repository.PassengerRepository;
import com.group3.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;

    public PassengerService(
            PassengerRepository passengerRepository,
            UserRepository userRepository
    ) {
        this.passengerRepository = passengerRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. CREATE PASSENGER
    // ==========================================

    @Transactional
    public PassengerResponse createPassenger(
            UUID userId,
            PassengerRequest request
    ) {

        User user = getActiveUser(userId);

        Passenger passenger = new Passenger(
                user,
                request.getFullName().trim(),
                normalizeOptional(request.getPassportNo()),
                request.getDob(),
                normalizeOptional(request.getPhone()),
                normalizeEmail(request.getEmail())
        );

        Passenger savedPassenger =
                passengerRepository.saveAndFlush(passenger);

        return new PassengerResponse(savedPassenger);
    }

    // ==========================================
    // 2. GET ALL PASSENGERS OF CURRENT USER
    // ==========================================

    @Transactional(readOnly = true)
    public List<PassengerResponse> getMyPassengers(
            UUID userId
    ) {

        getActiveUser(userId);

        List<Passenger> passengers =
                passengerRepository.findAllByUser_Id(userId);

        return passengers.stream()
                .map(PassengerResponse::new)
                .toList();
    }

    // ==========================================
    // 3. GET PASSENGER DETAILS
    // ==========================================

    @Transactional(readOnly = true)
    public PassengerResponse getPassengerById(
            UUID userId,
            UUID passengerId
    ) {

        getActiveUser(userId);

        Passenger passenger =
                getOwnedPassenger(userId, passengerId);

        return new PassengerResponse(passenger);
    }

    // ==========================================
    // 4. UPDATE PASSENGER
    // ==========================================

    @Transactional
    public PassengerResponse updatePassenger(
            UUID userId,
            UUID passengerId,
            PassengerRequest request
    ) {

        getActiveUser(userId);

        Passenger passenger =
                getOwnedPassenger(userId, passengerId);

        passenger.setFullName(
                request.getFullName().trim()
        );

        passenger.setPassportNo(
                normalizeOptional(request.getPassportNo())
        );

        passenger.setDob(request.getDob());

        passenger.setPhone(
                normalizeOptional(request.getPhone())
        );

        passenger.setEmail(
                normalizeEmail(request.getEmail())
        );

        Passenger savedPassenger =
                passengerRepository.saveAndFlush(passenger);

        return new PassengerResponse(savedPassenger);
    }

    // ==========================================
    // 5. DELETE PASSENGER
    // ==========================================

    @Transactional
    public void deletePassenger(
            UUID userId,
            UUID passengerId
    ) {

        getActiveUser(userId);

        Passenger passenger =
                getOwnedPassenger(userId, passengerId);

        passengerRepository.delete(passenger);
    }

    // ==========================================
    // COMMON: CHECK ACTIVE USER
    // ==========================================

    private User getActiveUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                "USER_NOT_FOUND",
                                ErrorType.NOT_FOUND,
                                "User not found"
                        )
                );

        if (!user.isActive()) {
            throw new CustomException(
                    "ACCOUNT_DISABLED",
                    ErrorType.FORBIDDEN,
                    "This account has been disabled"
            );
        }

        return user;
    }

    // ==========================================
    // COMMON: CHECK PASSENGER OWNERSHIP
    // ==========================================

    private Passenger getOwnedPassenger(
            UUID userId,
            UUID passengerId
    ) {

        return passengerRepository
                .findByIdAndUser_Id(passengerId, userId)
                .orElseThrow(() ->
                        new CustomException(
                                "PASSENGER_NOT_FOUND",
                                ErrorType.NOT_FOUND,
                                "Passenger not found"
                        )
                );
    }

    // ==========================================
    // COMMON: NORMALIZE OPTIONAL TEXT
    // ==========================================

    private String normalizeOptional(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    // ==========================================
    // COMMON: NORMALIZE EMAIL
    // ==========================================

    private String normalizeEmail(String email) {

        String normalized = normalizeOptional(email);

        if (normalized == null) {
            return null;
        }

        return normalized.toLowerCase(Locale.ROOT);
    }
}
