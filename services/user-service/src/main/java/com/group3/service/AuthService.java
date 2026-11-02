
package com.group3.service;

import com.group3.dto.auth.AuthResponse;
import com.group3.dto.auth.LoginRequest;
import com.group3.dto.auth.RegisterRequest;
import com.group3.entity.User;
import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.repository.UserRepository;
import com.group3.security.JwtService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ========================================
    // 1. REGISTER
    // ========================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Normalize email
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Check duplicate email
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }

        // Hash password using BCrypt
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // Normalize optional phone
        String phone = request.getPhone();

        if (phone != null) {
            phone = phone.trim();

            if (phone.isEmpty()) {
                phone = null;
            }
        }

        // Create new customer
        User user = new User(
                email,
                encodedPassword,
                phone
        );

        // Public registration can only create CUSTOMER
        user.setRole("CUSTOMER");
        user.setActive(true);

        User savedUser;

        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            // Handle concurrent registrations with same email
            throw new CustomException(
                    "EMAIL_ALREADY_EXISTS",
                    ErrorType.CONFLICT,
                    "Email is already registered"
            );
        }

        // Generate JWT after successful creation
        String accessToken =
                jwtService.generateAccessToken(savedUser);

        return new AuthResponse(
                accessToken,
                jwtService.getExpirationSeconds(),
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    // ========================================
    // 2. LOGIN
    // ========================================

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Find account by email
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new CustomException(
                                "INVALID_CREDENTIALS",
                                ErrorType.UNAUTHORIZED,
                                "Invalid email or password"
                        )
                );

        // Verify password
        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new CustomException(
                    "INVALID_CREDENTIALS",
                    ErrorType.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Check account status
        if (!user.isActive()) {
            throw new CustomException(
                    "ACCOUNT_DISABLED",
                    ErrorType.FORBIDDEN,
                    "This account has been disabled"
            );
        }

        // Generate JWT
        String accessToken =
                jwtService.generateAccessToken(user);

        return new AuthResponse(
                accessToken,
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
