package com.group3.api.rest;

import com.group3.api.dto.user.*;
import com.group3.application.command.ChangePasswordCommand;
import com.group3.application.command.UpdateProfileCommand;
import com.group3.application.port.in.UserProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserProfileUseCase service;
    public UserController(UserProfileUseCase service) { this.service = service; }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(UserProfileResponse.from(service.getMyProfile(UUID.fromString(jwt.getSubject()))));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(UserProfileResponse.from(service.updateMyProfile(UUID.fromString(jwt.getSubject()),
                new UpdateProfileCommand(request.email(), request.phone()))));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(UUID.fromString(jwt.getSubject()),
                new ChangePasswordCommand(request.currentPassword(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }
}
