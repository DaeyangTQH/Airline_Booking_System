package com.group3.api.dto.admin;
import com.group3.domain.aggregate.User;
import java.util.UUID;
public record AdminUserResponse(UUID id, String email, String phone, String role, boolean active) {
    public static AdminUserResponse from(User u) {
        return new AdminUserResponse(u.getId().value(), u.getEmail().value(), u.getPhone(),
                u.getRole().name(), u.isActive());
    }
}
