package com.group3.api.dto.auth;
import com.group3.application.model.AuthResult;
import java.util.UUID;
public record AuthResponse(String accessToken, String tokenType, long expiresIn,
                           UUID userId, String email, String role) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.token().accessToken(), "Bearer", result.token().expiresIn(),
                result.user().getId().value(), result.user().getEmail().value(),
                result.user().getRole().name());
    }
}
