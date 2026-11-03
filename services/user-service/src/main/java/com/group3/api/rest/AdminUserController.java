package com.group3.api.rest;

import com.group3.api.dto.admin.AdminUserPageResponse;
import com.group3.api.dto.admin.AdminUserResponse;
import com.group3.api.dto.admin.UpdateUserStatusRequest;
import com.group3.application.port.in.AdminUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserUseCase service;
    public AdminUserController(AdminUserUseCase service) { this.service = service; }

    @GetMapping
    public ResponseEntity<AdminUserPageResponse> list(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam(defaultValue = "") String keyword,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        var result = service.list(UUID.fromString(jwt.getSubject()), keyword, page, size);
        return ResponseEntity.ok(new AdminUserPageResponse(
                result.items().stream().map(AdminUserResponse::from).toList(), result.page(),
                result.size(), result.totalElements(), result.totalPages()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ResponseEntity.ok(AdminUserResponse.from(service.get(UUID.fromString(jwt.getSubject()), id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> status(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                                    @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(AdminUserResponse.from(
                service.updateStatus(UUID.fromString(jwt.getSubject()), id, request.active())));
    }
}
