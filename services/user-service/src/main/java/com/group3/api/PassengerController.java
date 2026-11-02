
package com.group3.api;

import com.group3.dto.passenger.PassengerRequest;
import com.group3.dto.passenger.PassengerResponse;
import com.group3.service.PassengerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(
            PassengerService passengerService
    ) {
        this.passengerService = passengerService;
    }

    // ==========================================
    // 1. CREATE PASSENGER
    // POST /api/passengers
    // ==========================================

    @PostMapping
    public ResponseEntity<PassengerResponse> createPassenger(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PassengerRequest request
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PassengerResponse response =
                passengerService.createPassenger(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ==========================================
    // 2. GET MY PASSENGERS
    // GET /api/passengers
    // ==========================================

    @GetMapping
    public ResponseEntity<List<PassengerResponse>> getMyPassengers(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<PassengerResponse> response =
                passengerService.getMyPassengers(userId);

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 3. GET PASSENGER BY ID
    // GET /api/passengers/{id}
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponse> getPassengerById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID passengerId
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PassengerResponse response =
                passengerService.getPassengerById(
                        userId,
                        passengerId
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 4. UPDATE PASSENGER
    // PUT /api/passengers/{id}
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<PassengerResponse> updatePassenger(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID passengerId,
            @Valid @RequestBody PassengerRequest request
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PassengerResponse response =
                passengerService.updatePassenger(
                        userId,
                        passengerId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 5. DELETE PASSENGER
    // DELETE /api/passengers/{id}
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassenger(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID passengerId
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        passengerService.deletePassenger(
                userId,
                passengerId
        );

        return ResponseEntity.noContent().build();
    }
}
