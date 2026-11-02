
package com.group3.application.service;

import com.group3.application.command.PassengerCommand;
import com.group3.application.port.in.PassengerUseCase;
import com.group3.application.port.out.PassengerRepositoryPort;
import com.group3.application.port.out.UserRepositoryPort;

import com.group3.domain.aggregate.Passenger;
import com.group3.domain.aggregate.User;
import com.group3.domain.vo.UserId;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PassengerService implements PassengerUseCase {

    private final PassengerRepositoryPort passengerRepository;
    private final UserRepositoryPort userRepository;

    public PassengerService(
            PassengerRepositoryPort passengerRepository,
            UserRepositoryPort userRepository
    ) {
        this.passengerRepository = passengerRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. CREATE PASSENGER
    // ==========================================

    @Override
    @Transactional
    public Passenger create(
            UUID userId,
            PassengerCommand command
    ) {

        // Verify current user is active
        getActiveUser(userId);

        // Create passenger using Domain Model
        Passenger passenger = Passenger.create(
                new UserId(userId),
                command.fullName(),
                command.passportNo(),
                command.dob(),
                command.phone(),
                command.email()
        );

        // Save through Repository Port
        return passengerRepository.save(passenger);
    }

    // ==========================================
    // 2. GET ALL PASSENGERS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Passenger> list(UUID userId) {

        getActiveUser(userId);

        return passengerRepository.findAllOwned(userId);
    }

    // ==========================================
    // 3. GET PASSENGER BY ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Passenger get(
            UUID userId,
            UUID passengerId
    ) {

        getActiveUser(userId);

        return getOwnedPassenger(userId, passengerId);
    }

    // ==========================================
    // 4. UPDATE PASSENGER
    // ==========================================

    @Override
    @Transactional
    public Passenger update(
            UUID userId,
            UUID passengerId,
            PassengerCommand command
    ) {

        getActiveUser(userId);

        // Verify passenger belongs to current user
        Passenger passenger =
                getOwnedPassenger(userId, passengerId);

        // Domain Model handles validation
        passenger.update(
                command.fullName(),
                command.passportNo(),
                command.dob(),
                command.phone(),
                command.email()
        );

        // Persist updated domain model
        return passengerRepository.save(passenger);
    }

    // ==========================================
    // 5. DELETE PASSENGER
    // ==========================================

    @Override
    @Transactional
    public void delete(
            UUID userId,
            UUID passengerId
    ) {

        getActiveUser(userId);

        // Verify ownership before deletion
        Passenger passenger =
                getOwnedPassenger(userId, passengerId);

        passengerRepository.delete(passenger);
    }

    // ==========================================
    // COMMON: GET ACTIVE USER
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
    // COMMON: VERIFY PASSENGER OWNERSHIP
    // ==========================================

    private Passenger getOwnedPassenger(
            UUID userId,
            UUID passengerId
    ) {

        return passengerRepository
                .findOwned(passengerId, userId)
                .orElseThrow(() ->
                        new CustomException(
                                "PASSENGER_NOT_FOUND",
                                ErrorType.NOT_FOUND,
                                "Passenger not found"
                        )
                );
    }
}
