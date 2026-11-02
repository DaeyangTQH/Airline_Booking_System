
package com.group3.api;

import com.group3.dto.user.ChangePasswordRequest;
import com.group3.dto.user.UpdateProfileRequest;
import com.group3.dto.user.UserProfileResponse;
import com.group3.service.UserProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(
            UserProfileService userProfileService
    ) {
        this.userProfileService = userProfileService;
    }

    // ==========================================
    // 1. GET /api/users/me
    // ==========================================

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        UserProfileResponse response =
                userProfileService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. PUT /api/users/me
    // ==========================================

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        UserProfileResponse response =
                userProfileService.updateMyProfile(
                        userId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 3. PUT /api/users/me/password
    // ==========================================

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        userProfileService.changePassword(
                userId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}
