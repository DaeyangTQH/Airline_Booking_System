package com.group3.api.rest;

import com.group3.api.dto.passenger.BookingValidationRequest;
import com.group3.api.dto.passenger.BookingValidationResponse;
import com.group3.api.dto.passenger.PassengerResponse;
import com.group3.application.port.in.BookingValidationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/passengers/booking")
public class BookingValidationController {
    private final BookingValidationUseCase service;
    public BookingValidationController(BookingValidationUseCase service) { this.service = service; }

    @PostMapping("/validate")
    public ResponseEntity<BookingValidationResponse> validate(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody BookingValidationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var passengers = service.validate(userId, request.passengerIds()).stream().map(PassengerResponse::from).toList();
        return ResponseEntity.ok(new BookingValidationResponse(userId, passengers.size(), passengers));
    }
}
