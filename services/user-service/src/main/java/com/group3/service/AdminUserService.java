
package com.group3.service;

import com.group3.dto.admin.AdminUserPageResponse;
import com.group3.dto.admin.AdminUserResponse;
import com.group3.dto.admin.UpdateUserStatusRequest;

import com.group3.entity.User;
import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. GET ALL USERS + SEARCH + PAGINATION
    // ==========================================

    @Transactional(readOnly = true)
    public AdminUserPageResponse getAllUsers(
            UUID adminId,
            String keyword,
            int page,
            int size
    ) {

        requireActiveAdmin(adminId);

        if (page < 0 || size < 1 || size > 100) {
            throw new CustomException(
                    "INVALID_PAGINATION",
                    ErrorType.VALIDATION,
                    "Page must be >= 0 and size must be 1-100"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "email")
        );

        String search = keyword == null
                ? ""
                : keyword.trim();

        Page<User> userPage;

        if (search.isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage =
                    userRepository.findByEmailContainingIgnoreCase(
                            search,
                            pageable
                    );
        }

        List<AdminUserResponse> users =
                userPage.getContent()
                        .stream()
                        .map(AdminUserResponse::new)
                        .toList();

        return new AdminUserPageResponse(
                users,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    // ==========================================
    // 2. GET USER DETAILS
    // ==========================================

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(
            UUID adminId,
            UUID userId
    ) {

        requireActiveAdmin(adminId);

        User user = findUser(userId);

        return new AdminUserResponse(user);
    }

    // ==========================================
    // 3. LOCK / UNLOCK USER ACCOUNT
    // ==========================================

    @Transactional
    public AdminUserResponse updateUserStatus(
            UUID adminId,
            UUID userId,
            UpdateUserStatusRequest request
    ) {

        requireActiveAdmin(adminId);

        User targetUser = findUser(userId);

        boolean newStatus = request.getActive();

        // Do not allow changing another admin's status
        if ("ADMIN".equalsIgnoreCase(targetUser.getRole())) {
            throw new CustomException(
                    "ADMIN_ACCOUNT_PROTECTED",
                    ErrorType.FORBIDDEN,
                    "Admin accounts cannot be changed here"
            );
        }

        // Explicitly prevent self-locking
        if (adminId.equals(userId) && !newStatus) {
            throw new CustomException(
                    "CANNOT_DISABLE_SELF",
                    ErrorType.FORBIDDEN,
                    "You cannot disable your own account"
            );
        }

        targetUser.setActive(newStatus);

        User savedUser =
                userRepository.saveAndFlush(targetUser);

        return new AdminUserResponse(savedUser);
    }

    // ==========================================
    // COMMON: VERIFY CURRENT ADMIN
    // ==========================================

    private void requireActiveAdmin(UUID adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new CustomException(
                                "ACCESS_DENIED",
                                ErrorType.FORBIDDEN,
                                "Admin access is required"
                        )
                );

        if (!admin.isActive()
                || !"ADMIN".equals(admin.getRole())) {

            throw new CustomException(
                    "ACCESS_DENIED",
                    ErrorType.FORBIDDEN,
                    "Admin access is required"
            );
        }
    }

    // ==========================================
    // COMMON: FIND TARGET USER
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
