
package com.group3.api;

import com.group3.dto.admin.AdminUserPageResponse;
import com.group3.dto.admin.AdminUserResponse;
import com.group3.dto.admin.UpdateUserStatusRequest;
import com.group3.service.AdminUserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(
            AdminUserService adminUserService
    ) {
        this.adminUserService = adminUserService;
    }

    // ==========================================
    // 1. GET ALL USERS / SEARCH / PAGINATION
    // ==========================================

    @GetMapping
    public ResponseEntity<AdminUserPageResponse> getAllUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        UUID adminId = UUID.fromString(jwt.getSubject());

        AdminUserPageResponse response =
                adminUserService.getAllUsers(
                        adminId,
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. GET USER BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID userId
    ) {

        UUID adminId = UUID.fromString(jwt.getSubject());

        AdminUserResponse response =
                adminUserService.getUserById(
                        adminId,
                        userId
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 3. LOCK / UNLOCK USER ACCOUNT
    // ==========================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        UUID adminId = UUID.fromString(jwt.getSubject());

        AdminUserResponse response =
                adminUserService.updateUserStatus(
                        adminId,
                        userId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}
