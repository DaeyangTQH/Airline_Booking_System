
package com.group3.application.service;

import com.group3.application.command.LoginCommand;
import com.group3.application.command.RegisterCommand;
import com.group3.application.model.AuthResult;

import com.group3.application.port.in.AuthUseCase;
import com.group3.application.port.out.AccessTokenPort;
import com.group3.application.port.out.PasswordHasherPort;
import com.group3.application.port.out.UserRepositoryPort;

import com.group3.domain.aggregate.User;
import com.group3.domain.vo.Email;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final AccessTokenPort accessToken;

    public AuthService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            AccessTokenPort accessToken
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.accessToken = accessToken;
    }

    // ==========================================
    // 1. REGISTER
    // ==========================================

    @Override
    @Transactional
    public AuthResult register(RegisterCommand command) {

        // Validate and normalize email
        Email email = new Email(command.email());

        String password = command.password();

        // Validate password
        if (password == null
                || password.length() < 8
                || password.length() > 72) {

            throw new CustomException(
                    "INVALID_PASSWORD",
                    ErrorType.VALIDATION,
                    "Password must be 8-72 characters"
            );
        }

        // Check duplicate email
        if (userRepository.existsByEmail(email.value())) {

            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }

        // Hash password using PasswordHasherPort
        String passwordHash =
                passwordHasher.encode(password);

        // Create customer through Domain Model
        User newUser = User.register(
                email,
                passwordHash,
                command.phone()
        );

        // Save user using Repository Port
        User savedUser;

        try {
            savedUser = userRepository.save(newUser);

        } catch (DataIntegrityViolationException ex) {

            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }

        // Generate JWT through AccessTokenPort
        return new AuthResult(
                savedUser,
                accessToken.issue(savedUser)
        );
    }

    // ==========================================
    // 2. LOGIN
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {

        String normalizedEmail;

        // Normalize email
        try {
            normalizedEmail =
                    new Email(command.email()).value();

        } catch (RuntimeException ex) {
            throw invalidCredentials();
        }

        // Find user through Repository Port
        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        // Verify password
        if (command.password() == null
                || !passwordHasher.matches(
                command.password(),
                user.getPasswordHash()
        )) {

            throw invalidCredentials();
        }

        // Check account status
        if (!user.isActive()) {

            throw new CustomException(
                    "ACCOUNT_DISABLED",
                    ErrorType.FORBIDDEN,
                    "This account has been disabled"
            );
        }

        // Issue access token
        return new AuthResult(
                user,
                accessToken.issue(user)
        );
    }

    // ==========================================
    // COMMON: INVALID CREDENTIALS
    // ==========================================

    private CustomException invalidCredentials() {

        return new CustomException(
                "INVALID_CREDENTIALS",
                ErrorType.UNAUTHORIZED,
                "Invalid email or password"
        );
    }
}
