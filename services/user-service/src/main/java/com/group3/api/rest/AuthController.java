package com.group3.api.rest;

import com.group3.api.dto.auth.*;
import com.group3.application.command.LoginCommand;
import com.group3.application.command.RegisterCommand;
import com.group3.application.port.in.AuthUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthUseCase service;
    public AuthController(AuthUseCase service) { this.service = service; }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(
                service.register(new RegisterCommand(request.email(), request.password(), request.phone()))));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(AuthResponse.from(
                service.login(new LoginCommand(request.email(), request.password()))));
    }
}
