package com.group3.infrastructure.security;

import com.group3.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashAdapter implements PasswordHasherPort {
    private final PasswordEncoder encoder;
    public PasswordHashAdapter(PasswordEncoder encoder) { this.encoder = encoder; }
    @Override public String encode(String rawPassword) { return encoder.encode(rawPassword); }
    @Override public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
