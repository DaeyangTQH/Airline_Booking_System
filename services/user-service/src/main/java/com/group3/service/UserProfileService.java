
package com.group3.service;

import com.group3.dto.user.ChangePasswordRequest;
import com.group3.dto.user.UpdateProfileRequest;
import com.group3.dto.user.UserProfileResponse;
import com.group3.entity.User;
import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // 1. GET CURRENT USER PROFILE
    // ==========================================

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UUID userId) {

        User user = getActiveUser(userId);

        return new UserProfileResponse(user);
    }

    // ==========================================
    // 2. UPDATE CURRENT USER PROFILE
    // ==========================================

    @Transactional
    public UserProfileResponse updateMyProfile(
            UUID userId,
            UpdateProfileRequest request
    ) {

        User user = getActiveUser(userId);

        String newEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Check whether the new email belongs to
        // another account
        boolean emailChanged =
                !user.getEmail().equalsIgnoreCase(newEmail);

        if (emailChanged &&
                userRepository.existsByEmailIgnoreCase(newEmail)) {

            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }

        String phone = request.getPhone();

        if (phone != null) {
            phone = phone.trim();

            if (phone.isEmpty()) {
                phone = null;
            }
        }

        user.setEmail(newEmail);
        user.setPhone(phone);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return new UserProfileResponse(savedUser);

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

    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordRequest request
    ) {

        User user = getActiveUser(userId);

        boolean passwordCorrect =
                passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPasswordHash()
                );

        if (!passwordCorrect) {

            throw new CustomException(
                    "INVALID_CURRENT_PASSWORD",
                    ErrorType.UNAUTHORIZED,
                    "Current password is incorrect"
            );
        }

        boolean samePassword =
                passwordEncoder.matches(
                        request.getNewPassword(),
                        user.getPasswordHash()
                );

        if (samePassword) {

            throw new CustomException(
                    "SAME_PASSWORD",
                    ErrorType.VALIDATION,
                    "New password must be different"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.setPasswordHash(encodedPassword);

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
