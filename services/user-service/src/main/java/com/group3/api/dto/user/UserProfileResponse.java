
package com.group3.api.dto.user;

import com.group3.domain.aggregate.User;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String phone,
        String role,
        boolean active
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId().value(),
                user.getEmail().value(),
                user.getPhone(),
                user.getRole().name(),
                user.isActive()
        );
    }
}
