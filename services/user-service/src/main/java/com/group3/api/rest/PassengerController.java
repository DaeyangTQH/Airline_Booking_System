package com.group3.api.rest;

import com.group3.api.dto.passenger.PassengerRequest;
import com.group3.api.dto.passenger.PassengerResponse;
import com.group3.application.command.PassengerCommand;
import com.group3.application.port.in.PassengerUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {
    private final PassengerUseCase service;
    public PassengerController(PassengerUseCase service) { this.service = service; }

    private PassengerCommand command(PassengerRequest r) {
        return new PassengerCommand(r.fullName(), r.passportNo(), r.dob(), r.phone(), r.email());
    }

    @PostMapping
    public ResponseEntity<PassengerResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PassengerResponse.from(
                service.create(UUID.fromString(jwt.getSubject()), command(request))));
    }

    @GetMapping
    public ResponseEntity<List<PassengerResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.list(UUID.fromString(jwt.getSubject())).stream()
                .map(PassengerResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ResponseEntity.ok(PassengerResponse.from(service.get(UUID.fromString(jwt.getSubject()), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassengerResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                                    @Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.ok(PassengerResponse.from(
                service.update(UUID.fromString(jwt.getSubject()), id, command(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.delete(UUID.fromString(jwt.getSubject()), id);
        return ResponseEntity.noContent().build();
    }
}
