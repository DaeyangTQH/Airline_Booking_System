
package com.group3.application.service;

import com.group3.application.model.PageResult;
import com.group3.application.port.in.AdminUserUseCase;
import com.group3.application.port.out.UserRepositoryPort;

import com.group3.domain.aggregate.User;
import com.group3.domain.aggregate.UserRole;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminUserService implements AdminUserUseCase {

    private final UserRepositoryPort userRepository;

    public AdminUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. GET ALL USERS + SEARCH + PAGINATION
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> list(
            UUID adminId,
            String keyword,
            int page,
            int size
    ) {

        // Check current admin
        requireActiveAdmin(adminId);

        // Validate pagination
        if (page < 0 || size < 1 || size > 100) {
            throw new CustomException(
                    "INVALID_PAGINATION",
                    ErrorType.VALIDATION,
                    "Page must be >= 0 and size must be 1-100"
            );
        }

        // Normalize search keyword
        String search = keyword == null
                ? ""
                : keyword.trim();

        // Query through repository port
        return userRepository.search(
                search,
                page,
                size
        );
    }

    // ==========================================
    // 2. GET USER BY ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public User get(
            UUID adminId,
            UUID userId
    ) {

        requireActiveAdmin(adminId);

        return findUser(userId);
    }

    // ==========================================
    // 3. LOCK / UNLOCK USER
    // ==========================================

    @Override
    @Transactional
    public User updateStatus(
            UUID adminId,
            UUID userId,
            boolean active
    ) {

        // Verify admin permissions
        requireActiveAdmin(adminId);

        // Find target account
        User targetUser = findUser(userId);

        // Prevent admin from disabling their own account
        if (adminId.equals(userId) && !active) {
            throw new CustomException(
                    "CANNOT_DISABLE_SELF",
                    ErrorType.FORBIDDEN,
                    "You cannot disable your own account"
            );
        }

        // Protect ADMIN accounts
        if (targetUser.getRole() == UserRole.ADMIN) {
            throw new CustomException(
                    "ADMIN_ACCOUNT_PROTECTED",
                    ErrorType.FORBIDDEN,
                    "Admin accounts cannot be changed here"
            );
        }

        // Update account status through Domain
        targetUser.changeActive(active);

        // Save through Repository Port
        return userRepository.save(targetUser);
    }

    // ==========================================
    // COMMON: VERIFY CURRENT ADMIN
    // ==========================================

    private User requireActiveAdmin(UUID adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new CustomException(
                                "ACCESS_DENIED",
                                ErrorType.FORBIDDEN,
                                "Admin access is required"
                        )
                );

        // Check account status
        if (!admin.isActive()) {
            throw new CustomException(
                    "ACCESS_DENIED",
                    ErrorType.FORBIDDEN,
                    "Admin account is disabled"
            );
        }

        // Check ADMIN role
        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(
                    "ACCESS_DENIED",
                    ErrorType.FORBIDDEN,
                    "Admin access is required"
            );
        }

        return admin;
    }

    // ==========================================
    // COMMON: FIND USER
    // ==========================================

    private User findUser(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                "USER_NOT_FOUND",
                                ErrorType.NOT_FOUND,
                                "User not found"
                        )
                );
    }
}
