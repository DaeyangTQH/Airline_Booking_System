
package com.group3.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        @Size(max = 20)
        String phone

) {
}
