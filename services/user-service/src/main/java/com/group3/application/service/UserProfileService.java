
package com.group3.application.service;

import com.group3.application.command.ChangePasswordCommand;
import com.group3.application.command.UpdateProfileCommand;
import com.group3.application.port.in.UserProfileUseCase;
import com.group3.application.port.out.PasswordHasherPort;
import com.group3.application.port.out.UserRepositoryPort;

import com.group3.domain.aggregate.User;
import com.group3.domain.vo.Email;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileService implements UserProfileUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public UserProfileService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    // ==========================================
    // 1. GET CURRENT USER PROFILE
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public User getMyProfile(UUID userId) {

        return getActiveUser(userId);
    }

    // ==========================================
    // 2. UPDATE USER PROFILE
    // ==========================================

    @Override
    @Transactional
    public User updateMyProfile(
            UUID userId,
            UpdateProfileCommand command
    ) {

        User user = getActiveUser(userId);

        Email newEmail = new Email(command.email());

        // Check duplicate email
        if (!user.getEmail().equals(newEmail)
                && userRepository.existsByEmail(
                newEmail.value()
        )) {

            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }

        // Update domain object
        user.updateProfile(
                newEmail,
                command.phone()
        );

        try {
            return userRepository.save(user);

        } catch (DataIntegrityViolationException ex) {
            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }
    }

    // ==========================================
    // 3. CHANGE PASSWORD
    // ==========================================

    @Override
    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordCommand command
    ) {

        User user = getActiveUser(userId);

        // Verify current password
        boolean correctPassword =
                command.currentPassword() != null
                        && passwordHasher.matches(
                        command.currentPassword(),
                        user.getPasswordHash()
                );

        if (!correctPassword) {
            throw new CustomException(
                    "INVALID_CURRENT_PASSWORD",
                    ErrorType.UNAUTHORIZED,
                    "Current password is incorrect"
            );
        }

        String newPassword = command.newPassword();

        // Validate new password
        if (newPassword == null
                || newPassword.length() < 8
                || newPassword.length() > 72) {

            throw new CustomException(
                    "INVALID_NEW_PASSWORD",
                    ErrorType.VALIDATION,
                    "New password must be 8-72 characters"
            );
        }

        // Prevent reusing current password
        if (passwordHasher.matches(
                newPassword,
                user.getPasswordHash()
        )) {

            throw new CustomException(
                    "SAME_PASSWORD",
                    ErrorType.VALIDATION,
                    "New password must be different"
            );
        }

        // Update password hash in domain
        user.changePasswordHash(
                passwordHasher.encode(newPassword)
        );

        userRepository.save(user);
    }

    // ==========================================
    // COMMON: FIND ACTIVE USER
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
}
