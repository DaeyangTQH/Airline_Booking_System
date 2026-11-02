
package com.group3.api;

import com.group3.dto.passenger.BookingValidationRequest;
import com.group3.dto.passenger.BookingValidationResponse;
import com.group3.service.BookingValidationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/passengers/booking")
public class BookingValidationController {

    private final BookingValidationService bookingValidationService;

    public BookingValidationController(
            BookingValidationService bookingValidationService
    ) {
        this.bookingValidationService = bookingValidationService;
    }

    // ==========================================
    // POST /api/passengers/booking/validate
    // ==========================================

    @PostMapping("/validate")
    public ResponseEntity<BookingValidationResponse> validate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BookingValidationRequest request
    ) {

        // Get the authenticated user's ID from JWT
        UUID userId = UUID.fromString(jwt.getSubject());

        // Validate passenger ownership and return data
        BookingValidationResponse response =
                bookingValidationService.validateForBooking(
                        userId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}
